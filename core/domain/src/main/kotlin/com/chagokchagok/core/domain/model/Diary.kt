package com.chagokchagok.core.domain.model

/**
 * 진행 중인 기록의 현재 모습.
 *
 * @property savedVisits 평가까지 마친 방문 (다녀온 순서)
 * @property pendingVisit 도착했지만 아직 평가하지 않은 방문. 한 번에 하나만 있다. 없으면 null
 */
data class RecordProgress(
    val session: DriveSession,
    val savedVisits: List<VisitWithPlace>,
    val pendingVisit: VisitWithPlace?,
)

/**
 * 끝난 기록 = 완성된 일기 1권.
 *
 * @property visits 저장된 방문 (다녀온 순서)
 */
data class Diary(
    val session: DriveSession,
    val visits: List<VisitWithPlace>,
)

/**
 * 지금까지 쌓인 양.
 *
 * @property diaryCount 방문이 하나라도 있는 끝난 일기의 수
 * @property pinCount 그 일기들에 나오는 서로 다른 장소의 수
 */
data class DiarySummary(
    val diaryCount: Int,
    val pinCount: Int,
)
