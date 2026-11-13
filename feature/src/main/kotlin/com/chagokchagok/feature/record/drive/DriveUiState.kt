package com.chagokchagok.feature.record.drive

import com.chagokchagok.feature.common.RelationUi

/**
 * 주행 중 화면이 그리는 값 (화면 11).
 *
 * @property loaded 기록 상태를 한 번이라도 읽었으면 true
 * @property relation 진행 중인 기록의 관계. 기록 중이 아니면 null
 * @property placeNames 오늘 다녀온 곳 이름 (다녀온 순서)
 * @property pendingPlaceName 도착했지만 아직 평가하지 않은 곳. 없으면 null
 * @property endedSessionId [기록 종료]로 방금 끝낸 기록의 id. 끝내지 않았으면 null
 */
data class DriveUiState(
    val loaded: Boolean = false,
    val relation: RelationUi? = null,
    val placeNames: List<String> = emptyList(),
    val pendingPlaceName: String? = null,
    val endedSessionId: String? = null,
)
