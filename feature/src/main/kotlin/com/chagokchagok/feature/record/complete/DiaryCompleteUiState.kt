package com.chagokchagok.feature.record.complete

import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.record.memory.MemoryPromptUi

/**
 * 일기 완성 화면이 그리는 값 (화면 29).
 *
 * @property loaded 일기를 읽었으면 true
 * @property relation 그날의 관계. 일기를 찾지 못했으면 null
 * @property dateText 일기 날짜. 예: "2026.11.09"
 * @property visits 다녀온 곳 (다녀온 순서)
 * @property memoEditing 한마디 추가 팝업이 떠 있으면 그 상태, 아니면 null (화면 30)
 */
data class DiaryCompleteUiState(
    val loaded: Boolean = false,
    val relation: RelationUi? = null,
    val dateText: String = "",
    val visits: List<CompleteVisitUi> = emptyList(),
    val memoEditing: MemoryPromptUi.Memo? = null,
)

/**
 * 장소 카드 1장.
 *
 * @property arrivedAtText 도착 시각. 예: "11:20"
 * @property stayMinutes 머문 시간(분). 모르면 null
 * @property memo 남긴 한마디. 없으면 null
 */
data class CompleteVisitUi(
    val visitId: String,
    val placeName: String,
    val good: Boolean,
    val arrivedAtText: String,
    val stayMinutes: Long?,
    val memo: String?,
)
