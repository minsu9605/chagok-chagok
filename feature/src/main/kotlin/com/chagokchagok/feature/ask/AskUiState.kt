package com.chagokchagok.feature.ask

import com.chagokchagok.core.domain.ask.AskAnswerType
import com.chagokchagok.core.domain.ask.AskKind
import com.chagokchagok.feature.common.RelationUi

/**
 * 기억 물어보기가 그리는 값 (화면 12~19).
 *
 * @property stage 지금 단계. 닫혀 있으면 null
 * @property scopeRelation 지금 기록 중인 관계. 그 관계의 기억에서만 찾는다. 기록 중이 아니면 null (전체에서 찾음)
 */
data class AskUiState(
    val stage: AskStage? = null,
    val scopeRelation: RelationUi? = null,
)

sealed interface AskStage {
    /**
     * 듣는 중 (화면 12).
     *
     * @property text 지금까지 받아 적거나 손으로 적은 질문
     * @property listening 마이크로 듣고 있으면 true
     */
    data class Listening(val text: String = "", val listening: Boolean = false) : AskStage

    /**
     * 답변 (화면 13~17, 19).
     *
     * @property keywordLabel 찾는 말. 예: "치즈케이크 카페". 회상 질문이면 null
     * @property memory 보여 줄 기억. 기록이 없으면 null
     * @property moreCount [memory] 말고 더 있는 장소 수
     * @property badSkipped 별로였던 곳을 빼고 찾았으면 true
     */
    data class Answer(
        val question: String,
        val kind: AskKind,
        val type: AskAnswerType,
        val keywordLabel: String?,
        val memory: AskMemoryUi?,
        val moreCount: Int,
        val badSkipped: Boolean,
    ) : AskStage

    /** 새 장소 찾기 (화면 18). 지도 서비스를 붙이기 전이라 안내만 보여 준다. */
    data class Fresh(val keywordLabel: String?) : AskStage
}

/**
 * 탑승권으로 보여 줄 기억.
 *
 * @property dateText 다녀온 날짜. 예: "2026.10.12"
 * @property spokenDateText 소리로 읽을 날짜. 예: "10월 12일"
 * @property memo 그때 남긴 한마디. 없으면 null
 */
data class AskMemoryUi(
    val relation: RelationUi,
    val dateText: String,
    val spokenDateText: String,
    val placeName: String,
    val good: Boolean,
    val memo: String?,
)
