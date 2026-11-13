package com.chagokchagok.feature.record.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.navi.NaviController
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.feature.common.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    recordEngine: RecordEngine,
    private val naviController: NaviController,
) : ViewModel() {

    val uiState: StateFlow<DriveUiState> = recordEngine.progress
        .map { progress ->
            DriveUiState(
                loaded = true,
                relation = progress?.session?.relation?.toUi(),
                placeNames = progress?.savedVisits.orEmpty().map { it.place.name },
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

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
