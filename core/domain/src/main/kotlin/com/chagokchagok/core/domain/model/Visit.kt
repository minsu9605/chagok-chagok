package com.chagokchagok.core.domain.model

import java.time.Instant

/**
 * 방문 카드 1장. (DB 설계 2-2 `visit` 중 1차에 쓰는 값만)
 *
 * @property seq 그날 방문 순서. 1부터
 * @property rating 좋았어요·별로예요. [VisitStatus.SAVED]면 반드시 있다
 * @property memoText 한마디. 남기지 않았으면 null
 */
data class Visit(
    val id: String,
    val sessionId: String,
    val placeId: String,
    val seq: Int,
    val arrivedAt: Instant,
    val status: VisitStatus,
    val rating: Rating?,
    val memoText: String?,
)

enum class VisitStatus {
    /** 도착했고 아직 평가하지 않음. 평가 없이 닫으면 기록하지 않는다 */
    PENDING,

    /** 평가해서 저장됨 */
    SAVED,
}

enum class Rating { GOOD, BAD }

/** 방문과 그 장소를 함께 묶은 것. */
data class VisitWithPlace(
    val visit: Visit,
    val place: Place,
)
