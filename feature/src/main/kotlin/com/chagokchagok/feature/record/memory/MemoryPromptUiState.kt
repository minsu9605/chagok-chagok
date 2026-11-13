package com.chagokchagok.feature.record.memory

import com.chagokchagok.feature.common.RelationUi

/** 기억 남기기 팝업의 단계 (화면 23~28). 떠 있는 팝업이 없으면 null을 쓴다. */
sealed interface MemoryPromptUi {
    /**
     * ① 좋았어요·별로예요 (화면 23).
     *
     * @property arrivedAtText 도착 시각. 예: "13:05"
     * @property stayMinutes 머문 시간(분)
     * @property arrivedDateTimeText 도착 날짜와 시각. 저장 완료 화면에 쓴다. 예: "2026.11.09 13:05"
     */
    data class Rate(
        val visitId: String,
        val relation: RelationUi,
        val placeName: String,
        val arrivedAtText: String,
        val stayMinutes: Long,
        val arrivedDateTimeText: String,
    ) : MemoryPromptUi

    /**
     * ② 한마디 (화면 24 좋았어요, 25 별로예요).
     *
     * @property text 지금까지 받아 적거나 손으로 적은 한마디
     * @property listening 마이크로 듣고 있으면 true
     */
    data class Memo(
        val visitId: String,
        val placeName: String,
        val good: Boolean,
        val text: String,
        val listening: Boolean = false,
    ) : MemoryPromptUi

    /**
     * ③ 저장 완료 (화면 28). 해시태그는 아직 만들지 않는다.
     *
     * @property arrivedAtText 도착 날짜와 시각. 예: "2026.11.09 13:05"
     * @property memo 남긴 한마디. [다음에 말하기]를 눌렀으면 null
     */
    data class Saved(
        val relation: RelationUi,
        val placeName: String,
        val arrivedAtText: String,
        val good: Boolean,
        val memo: String?,
    ) : MemoryPromptUi
}
