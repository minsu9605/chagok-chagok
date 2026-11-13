package com.chagokchagok.core.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * 하루 기록 = 일기 1권. (DB 설계 2-1 `drive_session` 중 1차에 쓰는 값만)
 *
 * @property sessionDate 일기 날짜. 시작 시각 기준
 * @property endedAt 끝난 시각. 기록 중이면 null
 * @property relation 기록을 시작할 때 고른 관계. 끝날 때까지 바뀌지 않는다
 * @property initialOccupantCount 기록을 시작할 때의 탑승 인원
 */
data class DriveSession(
    val id: String,
    val sessionDate: LocalDate,
    val startedAt: Instant,
    val endedAt: Instant?,
    val status: SessionStatus,
    val relation: Relation,
    val initialOccupantCount: Int,
)

/** 함께 탄 사람과의 관계. */
enum class Relation { SOLO, COUPLE, FRIENDS, FAMILY }

/** 기록의 상태. 출발지 도착 자동 종료는 내비 위치를 연결할 때 더한다. */
enum class SessionStatus {
    /** 기록 중 */
    ACTIVE,

    /** [기록 종료]를 눌러 끝남 */
    ENDED_MANUAL,

    /** 끝내지 않은 채 마지막 갱신 뒤 3일이 지나 자동으로 마감됨 */
    ENDED_AUTO_STALE,
}
