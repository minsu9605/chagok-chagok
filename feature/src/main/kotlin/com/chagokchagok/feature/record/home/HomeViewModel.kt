package com.chagokchagok.feature.record.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.RecordProgress
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.toDomain
import com.chagokchagok.feature.common.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recordEngine: RecordEngine,
    private val diaryRepository: DiaryRepository,
    seatMonitor: SeatMonitor,
) : ViewModel() {

    /** 저장소에 없는, 이 화면만의 상태 (팝업과 토스트). */
    private data class LocalState(
        val relationPicker: RelationPickerUi? = null,
    )

    private val localState = MutableStateFlow(LocalState())

    private val _recordStarted = Channel<Boolean>(Channel.BUFFERED)

    /** 기록을 시작했을 때 한 번 낸다. 값은 좌석 1명이라 묻지 않고 시작했는지 여부. */
    val recordStarted: Flow<Boolean> = _recordStarted.receiveAsFlow()

    private val _recordEnded = Channel<String>(Channel.BUFFERED)

    /** 기록이 끝났을 때(직접 종료, 3일 자동 마감) 한 번 낸다. 값은 끝난 기록의 id. */
    val recordEnded: Flow<String> = _recordEnded.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        seatMonitor.occupantCount,
        recordEngine.progress,
        diaryRepository.observeLastDiary(),
        combine(
            diaryRepository.observeSummary(),
            diaryRepository.observeVisitCountOn(LocalDate.now()),
            ::Pair,
        ),
        localState,
    ) { seatCount, progress, lastDiary, (diarySummary, todayPlaceCount), local ->
        HomeUiState(
            dateText = LocalDate.now().format(TODAY_FORMATTER),
            seatCount = seatCount,
            todayPlaceCount = todayPlaceCount,
            lastRecord = lastDiary?.toLastRecordUi(),
            diarySummary = DiarySummaryUi(
                diaryCount = diarySummary.diaryCount,
                pinCount = diarySummary.pinCount,
            ),
            recording = progress?.toUi(),
            relationPicker = local.relationPicker,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(),
    )

    init {
        // 3일 넘게 끝내지 않은 기록이 있으면 마감하고, 남긴 곳이 있으면 그 일기의 완성 화면을 보여 준다.
        viewModelScope.launch {
            val closedSessionId = recordEngine.closeStaleRecord() ?: return@launch
            val hasVisits = diaryRepository.getDiary(closedSessionId)?.visits?.isNotEmpty() == true
            if (hasVisits) _recordEnded.send(closedSessionId)
        }
    }

    /** [쌓기 시작]: 좌석 1명이면 묻지 않고 '나 혼자'로 시작하고, 2명 이상이면 관계를 묻는다. */
    fun onStartRecordClick() {
        val seatCount = uiState.value.seatCount
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

    /** [기록 종료]. 기록을 끝내고, 끝난 기록의 id를 한 번 낸다. */
    fun onEndRecordClick() {
        viewModelScope.launch {
            val sessionId = recordEngine.end() ?: return@launch
            _recordEnded.send(sessionId)
        }
    }

    /** 관계 팝업의 [✕] */
    fun onRelationPickerClose() {
        localState.update { it.copy(relationPicker = null) }
    }

    private fun showRelationPicker(mode: RelationPickerMode, seatCount: Int) {
        localState.update { it.copy(relationPicker = RelationPickerUi(mode = mode, seatCount = seatCount)) }
    }

    private fun startRecord(relation: RelationUi, autoStarted: Boolean) {
        val seatCount = uiState.value.seatCount
        localState.update { it.copy(relationPicker = null) }
        viewModelScope.launch {
            recordEngine.start(relation = relation.toDomain(), occupantCount = seatCount)
            _recordStarted.send(autoStarted)
        }
    }

    private fun recommendedMode(seatCount: Int): RelationPickerMode =
        if (seatCount == 2) RelationPickerMode.TWO else RelationPickerMode.THREE_OR_MORE

    private fun Diary.toLastRecordUi() = LastRecordUi(
        dateText = session.sessionDate.format(MONTH_DAY_FORMATTER),
        relation = session.relation.toUi(),
        placeCount = visits.size,
    )

    private fun RecordProgress.toUi() = RecordingUi(
        relation = session.relation.toUi(),
        seatCount = session.initialOccupantCount,
        startedAtText = session.startedAt.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER),
        placeNames = savedVisits.map { it.place.name },
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val TODAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
        val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
    }
}
