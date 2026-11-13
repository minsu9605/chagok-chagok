package com.chagokchagok.feature.record.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.navi.NaviController
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.feature.common.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val recordEngine: RecordEngine,
    private val naviController: NaviController,
) : ViewModel() {

    private val endedSessionId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DriveUiState> = combine(recordEngine.progress, endedSessionId) { progress, endedId ->
            val visitCount = progress?.savedVisits.orEmpty().size
            DriveUiState(
                loaded = true,
                relation = progress?.session?.relation?.toUi(),
                placeNames = progress?.savedVisits.orEmpty().map { it.place.name },
                pendingPlaceName = progress?.pendingVisit?.place?.name,
                nextDevPlaceName = DEV_PLACE_NAMES[visitCount % DEV_PLACE_NAMES.size],
                endedSessionId = endedId,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DriveUiState(),
        )

    /** 주행 화면에 들어올 때 순정 내비를 앞으로 불러 달라고 요청한다. */
    fun onEnter() {
        naviController.showNavigationScreen()
    }

    /** [기록 종료]. 기록을 끝내고, 끝난 기록의 id를 화면에 알린다. */
    fun onEndRecordClick() {
        viewModelScope.launch { endedSessionId.value = recordEngine.end() }
    }

    /**
     * [개발자용] 내비의 목적지 도착을 흉내 낸다.
     * 실제 내비 도착 이벤트(NaviHelper `onDestinationArrived`)를 연결하면 삭제한다.
     */
    fun onDevArriveClick() {
        val placeName = uiState.value.nextDevPlaceName
        viewModelScope.launch { recordEngine.arrive(placeName = placeName, address = null) }
    }

    /** [개발자용] 시동을 다시 켠 것을 흉내 낸다. 실제 신호는 화면 켜짐 방송으로도 들어온다. */
    fun onDevRestartClick() {
        viewModelScope.launch { recordEngine.markReturned() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L

        // [개발자용] 도착을 흉내 낼 때 쓰는 장소 이름. 화면 설계 문서의 예시 장소다.
        val DEV_PLACE_NAMES = listOf("교동 뭉티기", "앞산 카페", "수성못")
    }
}
