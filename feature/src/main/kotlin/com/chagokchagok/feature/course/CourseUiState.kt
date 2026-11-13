package com.chagokchagok.feature.course

import com.chagokchagok.feature.common.RelationUi

/**
 * 코스 짜기 화면이 그리는 값 (화면 36~43).
 *
 * @property relation 코스를 짤 관계
 * @property step 지금 단계
 * @property heardText 받아 적거나 손으로 적은 첫 마디 (시작 단계에서만)
 * @property listening 마이크로 듣고 있으면 true
 * @property questionCount 지금까지 앱이 물은 횟수. 0이면 보이지 않는다
 * @property items 코스에 든 곳 (가는 순서). 코스 카드 단계에서만 찬다
 * @property swap 장소 바꾸기 팝업이 떠 있으면 그 상태, 아니면 null (화면 42-1)
 * @property swappedNote 방금 바꾼 것을 알리는 값: 몇 차를 어디로 바꿨는지. 없으면 null (화면 43)
 * @property recording 지금 기록 중이면 true. 출발하면 그 기록에 이어서 쌓는다
 */
data class CourseUiState(
    val relation: RelationUi = RelationUi.SOLO,
    val step: CourseStep = CourseStep.Start,
    val heardText: String = "",
    val listening: Boolean = false,
    val questionCount: Int = 0,
    val items: List<CourseItemUi> = emptyList(),
    val swap: CourseSwapUi? = null,
    val swappedNote: Pair<Int, String>? = null,
    val recording: Boolean = false,
)

/** 코스 짜기의 단계. [utterance]는 사용자가 한 말이다. */
sealed interface CourseStep {
    /** 시작: 어디로 갈지 묻는다 (화면 36) */
    data object Start : CourseStep

    /**
     * 내 기록 있음 (화면 37).
     *
     * @property keywordLabel 찾는 말. 없으면 null
     * @property memoryCount 맞는 좋았던 기억의 수
     */
    data class CaseA(val utterance: String, val keywordLabel: String?, val memoryCount: Int) : CourseStep

    /**
     * 다른 기록만 있음 (화면 38).
     *
     * @property memory 코스에 넣자고 권하는 기억
     */
    data class CaseB(val utterance: String, val keywordLabel: String, val memory: CourseItemUi) : CourseStep

    /** 새 후보 고르기 (화면 39) */
    data class NewCandidates(val utterance: String, val candidates: List<CourseItemUi>) : CourseStep

    /** 좋았던 기록 없음 (화면 40) */
    data class CaseC(val utterance: String) : CourseStep

    /** 시작 테마 고르기 (화면 41) */
    data class Themes(val utterance: String, val themes: List<CourseThemeUi>) : CourseStep

    /** 코스 완성 카드 (화면 42, 43) */
    data object Card : CourseStep
}

/**
 * 코스에 넣는 곳 하나.
 *
 * @property fromMemory 내 기억에서 왔으면 true, 새 장소면 false
 * @property dateText 내 기억이면 다녀온 날짜. 예: "2026.10.12"
 * @property category 새 장소의 업종. 예: "카페"
 * @property distanceText 새 장소의 거리. 예: "약 2km"
 */
data class CourseItemUi(
    val name: String,
    val fromMemory: Boolean,
    val dateText: String? = null,
    val category: String? = null,
    val distanceText: String? = null,
)

/**
 * 시작 테마.
 *
 * @property title 예: "물회집부터"
 * @property flow 예: "식사 → 바다 카페"
 */
data class CourseThemeUi(
    val title: String,
    val flow: String,
    val items: List<CourseItemUi>,
)

/**
 * 장소 바꾸기 팝업.
 *
 * @property index 바꿀 곳의 순서 (0부터)
 * @property candidates 바꿀 수 있는 후보
 */
data class CourseSwapUi(
    val index: Int,
    val candidates: List<CourseItemUi>,
)
