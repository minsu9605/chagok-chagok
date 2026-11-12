package com.chagokchagok.feature.record.home

import com.chagokchagok.feature.common.RelationUi

/**
 * 홈 화면이 그리는 값 (화면 02 기록 꺼짐, 10 기록 중).
 *
 * @property dateText 오늘 날짜. 예: "11월 9일 월요일"
 * @property seatCount 감지한 탑승 인원
 * @property todayPlaceCount 오늘 이미 끝난 기록에서 다녀온 곳 수
 * @property lastRecord 지난 기록 요약. 기록이 하나도 없으면 null
 * @property recording 기록 중이면 그 상태, 아니면 null
 */
data class HomeUiState(
    val dateText: String = "",
    val seatCount: Int = 0,
    val todayPlaceCount: Int = 0,
    val lastRecord: LastRecordUi? = null,
    val recording: RecordingUi? = null,
)

/**
 * 지난 기록 요약.
 *
 * @property dateText 예: "10월 24일"
 */
data class LastRecordUi(
    val dateText: String,
    val relation: RelationUi,
    val placeCount: Int,
)

/**
 * 기록 중 상태.
 *
 * @property startedAtText 시작 시각. 예: "09:40"
 * @property placeNames 오늘 다녀온 곳 이름 (다녀온 순서)
 */
data class RecordingUi(
    val relation: RelationUi,
    val seatCount: Int,
    val startedAtText: String,
    val placeNames: List<String>,
)
