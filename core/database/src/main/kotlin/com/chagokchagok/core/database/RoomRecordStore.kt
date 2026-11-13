package com.chagokchagok.core.database

import androidx.room.withTransaction
import com.chagokchagok.core.database.entity.DriveSessionEntity
import com.chagokchagok.core.database.entity.PlaceEntity
import com.chagokchagok.core.database.entity.VisitEntity
import com.chagokchagok.core.database.entity.VisitWithPlaceEntity
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.DiarySummary
import com.chagokchagok.core.domain.model.DriveSession
import com.chagokchagok.core.domain.model.Place
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.model.RecordProgress
import com.chagokchagok.core.domain.model.Relation
import com.chagokchagok.core.domain.model.SessionStatus
import com.chagokchagok.core.domain.model.Visit
import com.chagokchagok.core.domain.model.VisitStatus
import com.chagokchagok.core.domain.model.VisitWithPlace
import com.chagokchagok.core.domain.record.RecordEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 기록 엔진과 일기 조회의 Room 구현.
 * 바뀌는 즉시 DB에 적는다. 그래서 앱이 꺼졌다 켜져도 진행 중인 기록을 이어 간다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RoomRecordStore @Inject constructor(
    private val database: ChagokDatabase,
) : RecordEngine, DiaryRepository {
    private val dao = database.recordDao()

    override val progress: Flow<RecordProgress?> = dao.observeActiveSession().flatMapLatest { session ->
        if (session == null) {
            flowOf(null)
        } else {
            dao.observeVisits(session.sessionId).map { visits ->
                RecordProgress(
                    session = session.toDomain(),
                    savedVisits = visits.filter { it.visit.status == VisitStatus.SAVED.name }.map { it.toDomain() },
                    pendingVisit = visits.firstOrNull { it.visit.status == VisitStatus.PENDING.name }?.toDomain(),
                )
            }
        }
    }

    override suspend fun start(relation: Relation, occupantCount: Int): DriveSession =
        database.withTransaction {
            dao.getActiveSession()?.let { return@withTransaction it.toDomain() }
            val now = Instant.now()
            val session = DriveSessionEntity(
                sessionId = newId(),
                sessionDate = now.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                startedAt = now.toEpochMilli(),
                endedAt = null,
                status = SessionStatus.ACTIVE.name,
                relation = relation.name,
                initialOccupantCount = occupantCount,
                createdAt = now.toEpochMilli(),
                updatedAt = now.toEpochMilli(),
            )
            dao.insertSession(session)
            session.toDomain()
        }

    /** 도착 처리는 한 트랜잭션으로 묶는다 (DB 설계 4장). */
    override suspend fun arrive(placeName: String, address: String?): Visit? =
        database.withTransaction {
            val session = dao.getActiveSession() ?: return@withTransaction null
            val now = System.currentTimeMillis()
            // 평가하지 않은 이전 방문은 기록하지 않는다.
            dao.deletePendingVisits(session.sessionId)
            val place = dao.findPlaceByName(placeName) ?: PlaceEntity(
                placeId = newId(),
                name = placeName,
                address = address,
                createdAt = now,
                updatedAt = now,
            ).also { dao.insertPlace(it) }
            val visit = VisitEntity(
                visitId = newId(),
                sessionId = session.sessionId,
                placeId = place.placeId,
                seq = dao.countSavedVisits(session.sessionId) + 1,
                arrivedAt = now,
                returnedAt = null,
                status = VisitStatus.PENDING.name,
                rating = null,
                memoText = null,
                createdAt = now,
                updatedAt = now,
            )
            dao.insertVisit(visit)
            dao.touchSession(session.sessionId, now)
            visit.toDomain()
        }

    override suspend fun markReturned() {
        val session = dao.getActiveSession() ?: return
        dao.markPendingReturned(session.sessionId, System.currentTimeMillis())
    }

    override suspend fun rate(visitId: String, rating: Rating) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            dao.saveRating(visitId, rating.name, now)
            touchSessionOf(visitId, now)
        }
    }

    override suspend fun saveMemo(visitId: String, text: String) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            dao.saveMemo(visitId, text, now)
            touchSessionOf(visitId, now)
        }
    }

    override suspend fun discardPending(visitId: String) {
        dao.deletePendingVisit(visitId)
    }

    override suspend fun end(): String? =
        database.withTransaction {
            val session = dao.getActiveSession() ?: return@withTransaction null
            // 응답하지 않은 곳은 기록에서 뺀다.
            dao.deletePendingVisits(session.sessionId)
            dao.endSession(session.sessionId, SessionStatus.ENDED_MANUAL.name, System.currentTimeMillis())
            session.sessionId
        }

    override fun observeLastDiary(): Flow<Diary?> = dao.observeLastDiarySession().flatMapLatest { session ->
        if (session == null) {
            flowOf(null)
        } else {
            dao.observeVisits(session.sessionId).map { visits -> diaryOf(session, visits) }
        }
    }

    override fun observeSummary(): Flow<DiarySummary> =
        combine(dao.observeDiaryCount(), dao.observePinCount()) { diaryCount, pinCount ->
            DiarySummary(diaryCount = diaryCount, pinCount = pinCount)
        }

    override fun observeVisitCountOn(date: LocalDate): Flow<Int> = dao.observeSavedVisitCountOn(date.toString())

    override suspend fun getDiary(sessionId: String): Diary? {
        val session = dao.getSession(sessionId) ?: return null
        return diaryOf(session, dao.getVisits(sessionId))
    }

    override suspend fun saveDiaryMemo(visitId: String, text: String) = saveMemo(visitId, text)

    private suspend fun touchSessionOf(visitId: String, now: Long) {
        val sessionId = dao.getSessionIdOfVisit(visitId) ?: return
        dao.touchSession(sessionId, now)
    }

    private fun diaryOf(session: DriveSessionEntity, visits: List<VisitWithPlaceEntity>) = Diary(
        session = session.toDomain(),
        visits = visits.filter { it.visit.status == VisitStatus.SAVED.name }.map { it.toDomain() },
    )

    private fun newId(): String = UUID.randomUUID().toString()
}

private fun DriveSessionEntity.toDomain() = DriveSession(
    id = sessionId,
    sessionDate = LocalDate.parse(sessionDate),
    startedAt = Instant.ofEpochMilli(startedAt),
    endedAt = endedAt?.let(Instant::ofEpochMilli),
    status = SessionStatus.valueOf(status),
    relation = Relation.valueOf(relation),
    initialOccupantCount = initialOccupantCount,
)

private fun VisitEntity.toDomain() = Visit(
    id = visitId,
    sessionId = sessionId,
    placeId = placeId,
    seq = seq,
    arrivedAt = Instant.ofEpochMilli(arrivedAt),
    returnedAt = returnedAt?.let(Instant::ofEpochMilli),
    status = VisitStatus.valueOf(status),
    rating = rating?.let(Rating::valueOf),
    memoText = memoText,
)

private fun VisitWithPlaceEntity.toDomain() = VisitWithPlace(
    visit = visit.toDomain(),
    place = Place(id = place.placeId, name = place.name, address = place.address),
)
