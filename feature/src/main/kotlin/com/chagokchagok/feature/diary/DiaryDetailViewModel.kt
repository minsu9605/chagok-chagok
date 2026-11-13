package com.chagokchagok.feature.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.core.domain.voice.VoiceEvent
import com.chagokchagok.core.domain.voice.VoiceRecognizer
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.toDomain
import com.chagokchagok.feature.common.toUi
import com.chagokchagok.feature.navigation.DiaryDetailDestination
import com.chagokchagok.feature.record.home.RelationPickerMode
import com.chagokchagok.feature.record.home.RelationPickerUi
import com.chagokchagok.feature.record.memory.MemoryPromptUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** 일기 상세에서 화면을 옮겨야 할 때 한 번 내는 일. */
sealed interface DiaryDetailEvent {
    /** 일기가 지워졌다. 목록으로 돌아간다. */
    data object DiaryDeleted : DiaryDetailEvent

    /**
     * 다시 출발했다. 주행 화면으로 간다.
     *
     * @property newRecord 새 기록을 시작했으면 true, 하던 기록에 이어서 쌓으면 false
     * @property autoStarted 좌석 1명이라 묻지 않고 시작했으면 true
     */
    data class Departed(val newRecord: Boolean, val autoStarted: Boolean) : DiaryDetailEvent
}

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val recordEngine: RecordEngine,
    seatMonitor: SeatMonitor,
    private val voiceRecognizer: VoiceRecognizer,
) : ViewModel() {
    private val sessionId = savedStateHandle.toRoute<DiaryDetailDestination>().sessionId

    /** 저장소에 없는, 이 화면만의 상태 (팝업들). 일기 내용은 [uiState]를 만들 때 덮어쓴다. */
    private val overlays = MutableStateFlow(DiaryDetailUiState())
    private var listenJob: Job? = null

    /** 좌석마다 값이 따로 들어오므로 계속 지켜보다가 누를 때의 값을 쓴다. */
    private val seatCount = seatMonitor.occupantCount.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** 일기가 한 번이라도 보였는지. 보이던 일기가 사라지면 지워진 것이다. */
    private var shown = false

    private val _events = Channel<DiaryDetailEvent>(Channel.BUFFERED)
    val events: Flow<DiaryDetailEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<DiaryDetailUiState> = combine(
        diaryRepository.observeDiary(sessionId),
        recordEngine.progress,
        overlays,
    ) { diary, progress, overlay ->
        if (diary == null || diary.visits.isEmpty()) {
            if (shown) _events.trySend(DiaryDetailEvent.DiaryDeleted)
            DiaryDetailUiState(loaded = true)
        } else {
            shown = true
            overlay.withDiary(diary).copy(recording = progress != null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DiaryDetailUiState(),
    )

    // ── 한마디 추가 (화면 49) ────────────────────────────────────────────

    /** [한마디 추가하기]. 바로 듣기 시작한다. */
    fun onAddMemoClick(visitId: String, label: String) {
        val visit = visitOf(visitId) ?: return
        overlays.update {
            it.copy(
                memoEditing = MemoryPromptUi.Memo(
                    visitId = visitId,
                    placeName = label,
                    good = visit.good,
                    text = "",
                ),
            )
        }
        listen(
            onHeard = { text -> updateMemo { it.copy(text = text) } },
            onListening = { listening -> updateMemo { it.copy(listening = listening) } },
            onCompleted = ::onMemoSaveClick,
        )
    }

    /** 한마디를 손으로 고쳤다. 듣기는 멈춘다. */
    fun onMemoTextChange(text: String) {
        listenJob?.cancel()
        updateMemo { it.copy(text = text.take(MAX_MEMO_LENGTH), listening = false) }
    }

    fun onMemoSaveClick() {
        val editing = overlays.value.memoEditing ?: return
        saveMemo(visitId = editing.visitId, text = editing.text)
    }

    /** [다음에 말하기] */
    fun onMemoLaterClick() {
        listenJob?.cancel()
        overlays.update { it.copy(memoEditing = null) }
    }

    // ── 다시 말하기 (화면 50) ────────────────────────────────────────────

    /** [다시 말하기]. 바로 듣기 시작한다. */
    fun onRespeakClick(visitId: String) {
        val visit = visitOf(visitId) ?: return
        val memo = visit.memo ?: return
        overlays.update {
            it.copy(
                respeak = RespeakUi(
                    visitId = visitId,
                    number = visit.number,
                    placeName = visit.placeName,
                    currentMemo = memo,
                ),
            )
        }
        listen(
            onHeard = { text -> updateRespeak { it.copy(text = text) } },
            onListening = { listening -> updateRespeak { it.copy(listening = listening) } },
            onCompleted = ::onRespeakSaveClick,
        )
    }

    fun onRespeakTextChange(text: String) {
        listenJob?.cancel()
        updateRespeak { it.copy(text = text.take(MAX_MEMO_LENGTH), listening = false) }
    }

    fun onRespeakSaveClick() {
        val respeak = overlays.value.respeak ?: return
        saveMemo(visitId = respeak.visitId, text = respeak.text)
    }

    /** [닫기]. 지금 한마디는 그대로 둔다. */
    fun onRespeakClose() {
        listenJob?.cancel()
        overlays.update { it.copy(respeak = null) }
    }

    // ── 호불호 바꾸기 (화면 51) ──────────────────────────────────────────

    fun onRatingMenuClick(visitId: String) {
        overlays.update { it.copy(ratingMenuVisitId = if (it.ratingMenuVisitId == visitId) null else visitId) }
    }

    fun onRatingMenuDismiss() {
        overlays.update { it.copy(ratingMenuVisitId = null) }
    }

    fun onRatingPick(good: Boolean) {
        val visitId = overlays.value.ratingMenuVisitId ?: return
        overlays.update { it.copy(ratingMenuVisitId = null) }
        viewModelScope.launch {
            diaryRepository.changeRating(visitId, if (good) Rating.GOOD else Rating.BAD)
        }
    }

    // ── 삭제 (화면 52, 53) ──────────────────────────────────────────────

    fun onDeleteVisitClick(visitId: String) {
        val visit = visitOf(visitId) ?: return
        overlays.update { it.copy(deletingVisit = visit, ratingMenuVisitId = null) }
    }

    fun onDeleteVisitConfirm() {
        val visit = overlays.value.deletingVisit ?: return
        overlays.update { it.copy(deletingVisit = null) }
        viewModelScope.launch { diaryRepository.deleteVisit(visit.visitId) }
    }

    fun onDeleteDiaryClick() {
        overlays.update { it.copy(deletingDiary = true, ratingMenuVisitId = null) }
    }

    fun onDeleteDiaryConfirm() {
        overlays.update { it.copy(deletingDiary = false) }
        viewModelScope.launch { diaryRepository.deleteDiary(sessionId) }
    }

    fun onDeleteCancel() {
        overlays.update { it.copy(deletingVisit = null, deletingDiary = false) }
    }

    // ── 다시 출발 (화면 54, 55) ──────────────────────────────────────────

    /** [이 코스로 다시 출발하기]. 4곳 이상이면 갈 곳부터 고른다. */
    fun onRestartClick() {
        if (uiState.value.visits.size > StopPickerUi.MAX_STOPS) {
            overlays.update { it.copy(stopPicker = StopPickerUi(), ratingMenuVisitId = null) }
        } else {
            depart()
        }
    }

    /** [여기만 길 안내] */
    fun onSpotGuideClick() {
        depart()
    }

    /** 경유지 고르기에서 장소를 눌렀다. 고른 곳을 다시 누르면 빼고, 3곳이 차면 더 받지 않는다. */
    fun onStopClick(visitId: String) {
        overlays.update { state ->
            val picker = state.stopPicker ?: return@update state
            val picked = picker.pickedVisitIds
            val next = when {
                visitId in picked -> picked - visitId
                picked.size < StopPickerUi.MAX_STOPS -> picked + visitId
                else -> picked
            }
            state.copy(stopPicker = picker.copy(pickedVisitIds = next))
        }
    }

    /** [이 순서로 출발하기] */
    fun onStopsConfirm() {
        if (overlays.value.stopPicker?.pickedVisitIds.isNullOrEmpty()) return
        depart()
    }

    fun onRestartRelationClick(relation: RelationUi) {
        val picker = overlays.value.restartPicker ?: return
        overlays.update { it.copy(restartPicker = null) }
        viewModelScope.launch {
            startRecord(relation = relation, seatCount = picker.seatCount, autoStarted = false)
        }
    }

    /** 관계 확인 팝업의 [✕] */
    fun onRestartPickerClose() {
        overlays.update { it.copy(restartPicker = null) }
    }

    /**
     * 다시 출발한다. 이미 기록 중이면 관계를 묻지 않고 그 기록에 이어서 쌓는다.
     * 장소에 좌표가 없어서 갈 곳을 내비에 넣지는 못한다. 기록을 켜고 주행 화면으로 가는 데까지만 한다.
     */
    private fun depart() {
        viewModelScope.launch {
            if (recordEngine.progress.first() != null) {
                _events.send(DiaryDetailEvent.Departed(newRecord = false, autoStarted = false))
                return@launch
            }
            val seatCount = seatCount.value
            if (seatCount <= 1) {
                startRecord(relation = RelationUi.SOLO, seatCount = seatCount, autoStarted = true)
            } else {
                val mode = if (seatCount == 2) RelationPickerMode.TWO else RelationPickerMode.THREE_OR_MORE
                overlays.update { it.copy(restartPicker = RelationPickerUi(mode = mode, seatCount = seatCount)) }
            }
        }
    }

    private suspend fun startRecord(relation: RelationUi, seatCount: Int, autoStarted: Boolean) {
        recordEngine.start(relation = relation.toDomain(), occupantCount = seatCount)
        _events.send(DiaryDetailEvent.Departed(newRecord = true, autoStarted = autoStarted))
    }

    // ── 공통 ────────────────────────────────────────────────────────────

    private fun saveMemo(visitId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        listenJob?.cancel()
        overlays.update { it.copy(memoEditing = null, respeak = null) }
        viewModelScope.launch { diaryRepository.saveDiaryMemo(visitId, trimmed) }
    }

    /**
     * 마이크로 한 번 듣는다. 받아 적는 글을 [onHeard]로 넘기고, 말이 끝나면 [onCompleted]를 부른다.
     * 듣지 못하면 글자 입력으로 남긴다.
     */
    private fun listen(
        onHeard: (String) -> Unit,
        onListening: (Boolean) -> Unit,
        onCompleted: () -> Unit,
    ) {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            onListening(true)
            var finalText: String? = null
            voiceRecognizer.listen().collect { event ->
                when (event) {
                    is VoiceEvent.Heard -> {
                        onHeard(event.text.take(MAX_MEMO_LENGTH))
                        if (event.completed) finalText = event.text
                    }
                    VoiceEvent.Failed -> Unit
                }
            }
            onListening(false)
            if (!finalText.isNullOrBlank()) onCompleted()
        }
    }

    private fun updateMemo(transform: (MemoryPromptUi.Memo) -> MemoryPromptUi.Memo) {
        overlays.update { it.copy(memoEditing = it.memoEditing?.let(transform)) }
    }

    private fun updateRespeak(transform: (RespeakUi) -> RespeakUi) {
        overlays.update { it.copy(respeak = it.respeak?.let(transform)) }
    }

    private fun visitOf(visitId: String): DetailVisitUi? = uiState.value.visits.firstOrNull { it.visitId == visitId }

    private fun DiaryDetailUiState.withDiary(diary: Diary) = copy(
        loaded = true,
        relation = diary.session.relation.toUi(),
        dateText = diary.session.sessionDate.format(DATE_FORMATTER),
        monthDayText = diary.session.sessionDate.format(MONTH_DAY_FORMATTER),
        visits = diary.visits.mapIndexed { index, saved ->
            DetailVisitUi(
                visitId = saved.visit.id,
                number = index + 1,
                placeName = saved.place.name,
                good = saved.visit.rating == Rating.GOOD,
                memo = saved.visit.memoText?.takeIf { it.isNotBlank() },
            )
        },
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MAX_MEMO_LENGTH = 100
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
        val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
    }
}
