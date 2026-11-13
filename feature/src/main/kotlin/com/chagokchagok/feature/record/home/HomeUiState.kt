package com.chagokchagok.feature.record.home

import com.chagokchagok.feature.common.RelationUi

/**
 * 홈 화면이 그리는 값 (화면 02 기록 꺼짐, 10 기록 중).
 *
 * @property dateText 오늘 날짜. 예: "11월 9일 월요일"
 * @property seatCount 감지한 탑승 인원
 * @property todayPlaceCount 오늘 이미 끝난 기록에서 다녀온 곳 수
 * @property lastRecord 지난 기록 요약. 기록이 하나도 없으면 null
 * @property diarySummary 쌓인 일기와 핀의 수. 아직 모르면 null
 * @property recording 기록 중이면 그 상태, 아니면 null
 * @property relationPicker 관계 고르기 팝업이 떠 있으면 그 상태, 아니면 null (화면 05~07)
 * @property startToast 기록 시작 안내 토스트가 떠 있으면 그 상태, 아니면 null (화면 08, 09)
 */
data class HomeUiState(
    val dateText: String = "",
    val seatCount: Int = 0,
    val todayPlaceCount: Int = 0,
    val lastRecord: LastRecordUi? = null,
    val diarySummary: DiarySummaryUi? = null,
    val recording: RecordingUi? = null,
    val relationPicker: RelationPickerUi? = null,
    val startToast: StartToastUi? = null,
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

/** 일기 보기 위젯의 "일기 10권 · 핀 20곳". */
data class DiarySummaryUi(
    val diaryCount: Int,
    val pinCount: Int,
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

/** 관계 고르기 팝업의 종류. */
enum class RelationPickerMode {
    /** 좌석 2명: 연인·부부, 친구 (화면 05) */
    TWO,

    /** 좌석 3명 이상: 가족, 친구들 (화면 06) */
    THREE_OR_MORE,

    /** 다른 모드 직접 선택: 관계 4개 모두 (화면 07) */
    ALL,
}

/**
 * 관계 고르기 팝업.
 *
 * @property seatCount 팝업을 띄울 때 감지한 탑승 인원
 */
data class RelationPickerUi(
    val mode: RelationPickerMode,
    val seatCount: Int,
)

/**
 * 기록 시작 안내 토스트.
 *
 * @property autoStarted 좌석 1명이라 묻지 않고 바로 시작했으면 true (화면 09)
 */
data class StartToastUi(
    val relation: RelationUi,
    val autoStarted: Boolean,
)
