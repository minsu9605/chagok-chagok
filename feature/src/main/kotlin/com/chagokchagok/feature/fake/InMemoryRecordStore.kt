package com.chagokchagok.feature.fake

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/*
 * ─────────────────────────────────────────────────────────────────────────────
 * [임시 구현] 이 `fake` 패키지는 통째로 지울 코드입니다.
 *
 * - 왜 있나: 화면(feature)은 core:domain의 인터페이스로 동작합니다. 그 인터페이스의 실제 구현
 *   (core:database의 저장)을 만들기 전까지, 메모리 안에서만 동작하는 구현을 둡니다.
 * - 언제 지우나: core:database에 실제 구현이 생기면 이 패키지를 지우고,
 *   그 모듈이 인터페이스를 제공(DI)하도록 바꿉니다. 화면과 ViewModel은 고치지 않습니다.
 * - 주의: 저장하지 않으므로 앱을 다시 켜면 처음 상태(기록 없음)로 돌아갑니다.
 * ─────────────────────────────────────────────────────────────────────────────
 */

/** [임시 구현] 기록과 일기를 메모리에만 들고 있는 구현. core:database 연결 후 삭제. */
@Singleton
class InMemoryRecordStore @Inject constructor() : RecordEngine, DiaryRepository {
    private val active = MutableStateFlow<RecordProgress?>(null)
    private val diaries = MutableStateFlow<List<Diary>>(emptyList())

    override val progress: Flow<RecordProgress?> = active.asStateFlow()

    override suspend fun start(relation: Relation, occupantCount: Int): DriveSession {
        active.value?.let { return it.session }
        val now = Instant.now()
        val session = DriveSession(
            id = newId(),
            sessionDate = now.atZone(ZoneId.systemDefault()).toLocalDate(),
            startedAt = now,
            endedAt = null,
            status = SessionStatus.ACTIVE,
            relation = relation,
            initialOccupantCount = occupantCount,
        )
        active.value = RecordProgress(session = session, savedVisits = emptyList(), pendingVisit = null)
        return session
    }

    override suspend fun arrive(placeName: String, address: String?): Visit? {
        val current = active.value ?: return null
        val place = Place(id = newId(), name = placeName, address = address)
        val visit = Visit(
            id = newId(),
            sessionId = current.session.id,
            placeId = place.id,
            seq = current.savedVisits.size + 1,
            arrivedAt = Instant.now(),
            returnedAt = null,
            status = VisitStatus.PENDING,
            rating = null,
            memoText = null,
        )
        // 평가하지 않은 이전 방문은 새 방문으로 덮여 사라진다.
        active.value = current.copy(pendingVisit = VisitWithPlace(visit = visit, place = place))
        return visit
    }

    override suspend fun markReturned() {
        active.update { current ->
            val pending = current?.pendingVisit ?: return@update current
            if (pending.visit.returnedAt != null) return@update current
            current.copy(pendingVisit = pending.copy(visit = pending.visit.copy(returnedAt = Instant.now())))
        }
    }

    override suspend fun rate(visitId: String, rating: Rating) {
        active.update { current ->
            val pending = current?.pendingVisit?.takeIf { it.visit.id == visitId } ?: return@update current
            val saved = pending.copy(visit = pending.visit.copy(status = VisitStatus.SAVED, rating = rating))
            current.copy(savedVisits = current.savedVisits + saved, pendingVisit = null)
        }
    }

    override suspend fun saveMemo(visitId: String, text: String) {
        active.update { current ->
            current?.copy(
                savedVisits = current.savedVisits.map { saved ->
                    if (saved.visit.id == visitId) saved.copy(visit = saved.visit.copy(memoText = text)) else saved
                },
            )
        }
    }

    override suspend fun discardPending(visitId: String) {
        active.update { current ->
            if (current?.pendingVisit?.visit?.id == visitId) current.copy(pendingVisit = null) else current
        }
    }

    override suspend fun end(): String? {
        val current = active.value ?: return null
        val ended = current.session.copy(endedAt = Instant.now(), status = SessionStatus.ENDED_MANUAL)
        diaries.update { it + Diary(session = ended, visits = current.savedVisits) }
        active.value = null
        return ended.id
    }

    override fun observeLastDiary(): Flow<Diary?> =
        diaries.map { all -> all.lastOrNull { it.visits.isNotEmpty() } }

    override fun observeSummary(): Flow<DiarySummary> =
        diaries.map { all ->
            val withVisits = all.filter { it.visits.isNotEmpty() }
            DiarySummary(
                diaryCount = withVisits.size,
                pinCount = withVisits.flatMap { it.visits }.distinctBy { it.place.id }.size,
            )
        }

    override suspend fun getDiary(sessionId: String): Diary? =
        diaries.value.firstOrNull { it.session.id == sessionId }

    private fun newId(): String = UUID.randomUUID().toString()
}
