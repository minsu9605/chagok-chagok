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

/** 기록의 상태. 자동 종료(출발지 도착, 3일 경과)는 2차에서 더한다. */
enum class SessionStatus {
    /** 기록 중 */
    ACTIVE,

    /** [기록 종료]를 눌러 끝남 */
    ENDED_MANUAL,
}
