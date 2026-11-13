package com.chagokchagok.feature.diary

import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.record.home.RelationPickerUi
import com.chagokchagok.feature.record.memory.MemoryPromptUi

/**
 * 일기 상세 화면이 그리는 값 (화면 48~55).
 *
 * @property loaded 일기를 읽었으면 true
 * @property relation 그날의 관계. 일기를 아직 읽지 못했으면 null
 * @property dateText 일기 날짜. 예: "2026.11.09"
 * @property monthDayText 예: "11월 9일"
 * @property visits 다녀온 곳 (다녀온 순서)
 * @property recording 지금 기록 중이면 true. 다시 출발하면 그 기록에 이어서 쌓는다
 * @property memoEditing 한마디 추가 팝업 (화면 49)
 * @property respeak 다시 말하기 팝업 (화면 50)
 * @property ratingMenuVisitId 호불호 메뉴를 펼친 방문의 id (화면 51)
 * @property deletingVisit 삭제를 확인 중인 장소 (화면 52)
 * @property deletingDiary 일기 삭제를 확인 중이면 true (화면 53)
 * @property stopPicker 경유지 고르기 화면 (화면 54)
 * @property restartPicker 다시 출발의 관계 확인 팝업 (화면 55)
 */
data class DiaryDetailUiState(
    val loaded: Boolean = false,
    val relation: RelationUi? = null,
    val dateText: String = "",
    val monthDayText: String = "",
    val visits: List<DetailVisitUi> = emptyList(),
    val recording: Boolean = false,
    val memoEditing: MemoryPromptUi.Memo? = null,
    val respeak: RespeakUi? = null,
    val ratingMenuVisitId: String? = null,
    val deletingVisit: DetailVisitUi? = null,
    val deletingDiary: Boolean = false,
    val stopPicker: StopPickerUi? = null,
    val restartPicker: RelationPickerUi? = null,
)

/**
 * 장소 카드 1장.
 *
 * @property number 그날 방문 순서. 1부터
 * @property memo 남긴 한마디. 없으면 null
 */
data class DetailVisitUi(
    val visitId: String,
    val number: Int,
    val placeName: String,
    val good: Boolean,
    val memo: String?,
)

/**
 * 다시 말하기 팝업.
 *
 * @property currentMemo 지금 저장돼 있는 한마디
 * @property text 새로 받아 적거나 손으로 적은 한마디
 * @property listening 마이크로 듣고 있으면 true
 */
data class RespeakUi(
    val visitId: String,
    val number: Int,
    val placeName: String,
    val currentMemo: String,
    val text: String = "",
    val listening: Boolean = false,
)

/**
 * 경유지 고르기. 내비에는 경유지 2곳 + 목적지 1곳까지만 들어간다.
 *
 * @property pickedVisitIds 누른 순서대로. 마지막이 도착지다
 */
data class StopPickerUi(
    val pickedVisitIds: List<String> = emptyList(),
) {
    companion object {
        const val MAX_STOPS = 3
    }
}
