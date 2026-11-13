package com.chagokchagok.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.chagokchagok.core.database.entity.DriveSessionEntity
import com.chagokchagok.core.database.entity.PlaceEntity
import com.chagokchagok.core.database.entity.VisitEntity
import com.chagokchagok.core.database.entity.VisitWithPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    // ── 세션 ─────────────────────────────────────────────────────────────

    @Insert
    suspend fun insertSession(session: DriveSessionEntity)

    @Query("SELECT * FROM drive_session WHERE status = 'ACTIVE' ORDER BY started_at DESC LIMIT 1")
    fun observeActiveSession(): Flow<DriveSessionEntity?>

    @Query("SELECT * FROM drive_session WHERE status = 'ACTIVE' ORDER BY started_at DESC LIMIT 1")
    suspend fun getActiveSession(): DriveSessionEntity?

    @Query("SELECT * FROM drive_session WHERE session_id = :sessionId")
    suspend fun getSession(sessionId: String): DriveSessionEntity?

    @Query("UPDATE drive_session SET status = :status, ended_at = :endedAt, updated_at = :endedAt WHERE session_id = :sessionId")
    suspend fun endSession(sessionId: String, status: String, endedAt: Long)

    @Query("UPDATE drive_session SET updated_at = :now WHERE session_id = :sessionId")
    suspend fun touchSession(sessionId: String, now: Long)

    /** 가장 최근에 끝난 일기. 저장된 방문이 하나도 없는 일기는 건너뛴다. */
    @Query(
        """
        SELECT s.* FROM drive_session s
        WHERE s.status != 'ACTIVE'
          AND EXISTS (SELECT 1 FROM visit v WHERE v.session_id = s.session_id AND v.status = 'SAVED')
        ORDER BY s.ended_at DESC LIMIT 1
        """,
    )
    fun observeLastDiarySession(): Flow<DriveSessionEntity?>

    @Query(
        """
        SELECT COUNT(*) FROM drive_session s
        WHERE s.status != 'ACTIVE'
          AND EXISTS (SELECT 1 FROM visit v WHERE v.session_id = s.session_id AND v.status = 'SAVED')
        """,
    )
    fun observeDiaryCount(): Flow<Int>

    // ── 장소 ─────────────────────────────────────────────────────────────

    @Insert
    suspend fun insertPlace(place: PlaceEntity)

    @Query("SELECT * FROM place WHERE name = :name LIMIT 1")
    suspend fun findPlaceByName(name: String): PlaceEntity?

    /** 끝난 일기에 나오는 서로 다른 장소의 수. */
    @Query(
        """
        SELECT COUNT(DISTINCT v.place_id) FROM visit v
        JOIN drive_session s ON s.session_id = v.session_id
        WHERE s.status != 'ACTIVE' AND v.status = 'SAVED'
        """,
    )
    fun observePinCount(): Flow<Int>

    // ── 방문 ─────────────────────────────────────────────────────────────

    @Insert
    suspend fun insertVisit(visit: VisitEntity)

    @Transaction
    @Query("SELECT * FROM visit WHERE session_id = :sessionId ORDER BY seq, arrived_at")
    fun observeVisits(sessionId: String): Flow<List<VisitWithPlaceEntity>>

    @Transaction
    @Query("SELECT * FROM visit WHERE session_id = :sessionId ORDER BY seq, arrived_at")
    suspend fun getVisits(sessionId: String): List<VisitWithPlaceEntity>

    @Query("SELECT COUNT(*) FROM visit WHERE session_id = :sessionId AND status = 'SAVED'")
    suspend fun countSavedVisits(sessionId: String): Int

    @Query("SELECT session_id FROM visit WHERE visit_id = :visitId")
    suspend fun getSessionIdOfVisit(visitId: String): String?

    @Query("DELETE FROM visit WHERE session_id = :sessionId AND status = 'PENDING'")
    suspend fun deletePendingVisits(sessionId: String)

    @Query("DELETE FROM visit WHERE visit_id = :visitId AND status = 'PENDING'")
    suspend fun deletePendingVisit(visitId: String)

    @Query(
        """
        UPDATE visit SET returned_at = :now, updated_at = :now
        WHERE session_id = :sessionId AND status = 'PENDING' AND returned_at IS NULL
        """,
    )
    suspend fun markPendingReturned(sessionId: String, now: Long)

    @Query(
        """
        UPDATE visit SET status = 'SAVED', rating = :rating, updated_at = :now
        WHERE visit_id = :visitId AND status = 'PENDING'
        """,
    )
    suspend fun saveRating(visitId: String, rating: String, now: Long)

    @Query("UPDATE visit SET memo_text = :text, updated_at = :now WHERE visit_id = :visitId AND status = 'SAVED'")
    suspend fun saveMemo(visitId: String, text: String, now: Long)

    /** 그 날짜의 끝난 일기에 저장된 방문 수. */
    @Query(
        """
        SELECT COUNT(*) FROM visit v
        JOIN drive_session s ON s.session_id = v.session_id
        WHERE s.status != 'ACTIVE' AND v.status = 'SAVED' AND s.session_date = :sessionDate
        """,
    )
    fun observeSavedVisitCountOn(sessionDate: String): Flow<Int>
}
