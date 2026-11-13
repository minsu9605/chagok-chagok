package com.chagokchagok.feature.record.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.fake.FakeLastRecord
import com.chagokchagok.feature.fake.FakeRecordStore
import com.chagokchagok.feature.fake.FakeSession
import com.chagokchagok.feature.fake.toFakeRelation
import com.chagokchagok.feature.fake.toRelationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // [임시 가짜 구현] core:domain 연결 후 core:domain 인터페이스로 바꾼다.
    private val fakeRecordStore: FakeRecordStore,
) : ViewModel() {

    /** 저장소에 없는, 이 화면만의 상태 (팝업과 토스트). */
    private data class LocalState(
        val relationPicker: RelationPickerUi? = null,
        val startToast: StartToastUi? = null,
    )

    private val localState = MutableStateFlow(LocalState())
    private var startToastJob: Job? = null

    val uiState: StateFlow<HomeUiState> = combine(
        fakeRecordStore.seatCount,
        fakeRecordStore.activeSession,
        fakeRecordStore.lastRecord,
        fakeRecordStore.diarySummary,
        localState,
    ) { seatCount, activeSession, lastRecord, diarySummary, local ->
        HomeUiState(
            dateText = LocalDate.now().format(TODAY_FORMATTER),
            seatCount = seatCount,
            lastRecord = lastRecord?.toUi(),
            diarySummary = DiarySummaryUi(
                diaryCount = diarySummary.diaryCount,
                pinCount = diarySummary.pinCount,
            ),
            recording = activeSession?.toUi(),
            relationPicker = local.relationPicker,
            startToast = local.startToast,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(),
    )

    /** [쌓기 시작]: 좌석 1명이면 묻지 않고 '나 혼자'로 시작하고, 2명 이상이면 관계를 묻는다. */
    fun onStartRecordClick() {
        val seatCount = fakeRecordStore.seatCount.value
        if (seatCount <= 1) {
            startRecord(relation = RelationUi.SOLO, autoStarted = true)
        } else {
            showRelationPicker(mode = recommendedMode(seatCount), seatCount = seatCount)
        }
    }

    /** 관계 타일을 눌렀을 때. 고른 관계는 기록이 끝날 때까지 바뀌지 않는다. */
    fun onRelationClick(relation: RelationUi) {
        startRecord(relation = relation, autoStarted = false)
    }

    /** [다른 모드 직접 선택] */
    fun onShowAllRelationsClick() {
        val picker = localState.value.relationPicker ?: return
        showRelationPicker(mode = RelationPickerMode.ALL, seatCount = picker.seatCount)
    }

    /** [추천으로 돌아가기] */
    fun onBackToRecommendedClick() {
        val picker = localState.value.relationPicker ?: return
        showRelationPicker(mode = recommendedMode(picker.seatCount), seatCount = picker.seatCount)
    }

    /** 관계 팝업의 [✕] */
    fun onRelationPickerClose() {
        localState.update { it.copy(relationPicker = null) }
    }

    /** 시작 안내 토스트를 눌러 닫을 때. */
    fun onStartToastClick() {
        startToastJob?.cancel()
        localState.update { it.copy(startToast = null) }
    }

    /** [임시 가짜 구현] 탑승 인원 칩을 길게 누르면 인원을 바꾼다. core:pleos 좌석 감지 연결 후 삭제. */
    fun onSeatChipLongClick() {
        fakeRecordStore.cycleSeatCount()
    }

    private fun showRelationPicker(mode: RelationPickerMode, seatCount: Int) {
        localState.update { it.copy(relationPicker = RelationPickerUi(mode = mode, seatCount = seatCount)) }
    }

    private fun startRecord(relation: RelationUi, autoStarted: Boolean) {
        fakeRecordStore.startSession(relation.toFakeRelation())
        localState.update {
            it.copy(
                relationPicker = null,
                startToast = StartToastUi(relation = relation, autoStarted = autoStarted),
            )
        }
        startToastJob?.cancel()
        startToastJob = viewModelScope.launch {
            delay(START_TOAST_MILLIS)
            localState.update { it.copy(startToast = null) }
        }
    }

    private fun recommendedMode(seatCount: Int): RelationPickerMode =
        if (seatCount == 2) RelationPickerMode.TWO else RelationPickerMode.THREE_OR_MORE

    private fun FakeLastRecord.toUi() = LastRecordUi(
        dateText = date.format(MONTH_DAY_FORMATTER),
        relation = relation.toRelationUi(),
        placeCount = placeCount,
    )

    private fun FakeSession.toUi() = RecordingUi(
        relation = relation.toRelationUi(),
        seatCount = seatCount,
        startedAtText = startedAt.format(TIME_FORMATTER),
        placeNames = placeNames,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val START_TOAST_MILLIS = 4_000L
        val TODAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
        val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
    }
}
