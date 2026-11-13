package com.chagokchagok.feature.diary

import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.record.home.RelationPickerUi

/**
 * 일기 보기 화면이 그리는 값 (화면 44 전체, 45 관계 탭, 46 기록 0건, 47 첫 기록 진행 중).
 *
 * @property loaded 일기를 읽었으면 true
 * @property hasDiaries 탭과 상관없이 일기가 하나라도 있으면 true
 * @property tab 보고 있는 관계 탭. 전체면 null
 * @property diaries 탭에 맞는 일기 카드 (최근 것부터)
 * @property pins 탭에 맞는 핀
 * @property legend 전체 탭의 범례: 관계마다 다녀온 곳 수 (0곳인 관계는 없음)
 * @property recordingRelation 지금 기록 중이면 그 관계, 아니면 null
 * @property relationPicker 관계 고르기 팝업이 떠 있으면 그 상태, 아니면 null
 */
data class DiaryListUiState(
    val loaded: Boolean = false,
    val hasDiaries: Boolean = false,
    val tab: RelationUi? = null,
    val diaries: List<DiaryCardUi> = emptyList(),
    val pins: List<DiaryPinUi> = emptyList(),
    val legend: List<Pair<RelationUi, Int>> = emptyList(),
    val recordingRelation: RelationUi? = null,
    val relationPicker: RelationPickerUi? = null,
)

/**
 * 일기 카드 1장.
 *
 * @property dateText 예: "2026.11.09"
 * @property placeNames 다녀온 곳 이름 (다녀온 순서)
 * @property badCount 별로예요로 남긴 곳 수
 * @property missingMemoCount 한마디를 남기지 않은 곳 수
 */
data class DiaryCardUi(
    val sessionId: String,
    val dateText: String,
    val relation: RelationUi,
    val placeNames: List<String>,
    val badCount: Int,
    val missingMemoCount: Int,
)

/**
 * 핀 하나. 장소마다 가장 최근 기록의 관계 색으로 하나만 찍는다.
 *
 * @property slot 그림에서 핀을 놓을 자리의 번호. 탭을 바꿔도 같은 장소는 같은 자리에 있다
 * @property sessionId 누르면 열 일기
 */
data class DiaryPinUi(
    val placeId: String,
    val relation: RelationUi,
    val slot: Int,
    val sessionId: String,
)
