package com.chagokchagok.feature.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.ask.AskAnswer
import com.chagokchagok.core.domain.ask.AskAnswerType
import com.chagokchagok.core.domain.ask.AskKind
import com.chagokchagok.core.domain.ask.Memory
import com.chagokchagok.core.domain.ask.MemoryAsk
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.voice.VoiceEvent
import com.chagokchagok.core.domain.voice.VoiceRecognizer
import com.chagokchagok.core.domain.voice.VoiceSpeaker
import com.chagokchagok.feature.common.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

/**
 * 기억 물어보기의 흐름: 듣기 → 남긴 기억에서 찾기 → 답변.
 * 주행 화면의 미니 패널과 홈의 팝업이 같이 쓴다.
 */
@HiltViewModel
class AskViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val recordEngine: RecordEngine,
    private val voiceRecognizer: VoiceRecognizer,
    private val voiceSpeaker: VoiceSpeaker,
) : ViewModel() {

    private val stage = MutableStateFlow<AskStage?>(null)
    private var listenJob: Job? = null
    private var autoCloseJob: Job? = null

    private val _guideRequests = Channel<String>(Channel.BUFFERED)

    /** 답으로 나온 곳으로 길 안내를 골랐을 때 한 번 낸다. 값은 그 장소의 이름. */
    val guideRequests: Flow<String> = _guideRequests.receiveAsFlow()

    val uiState: StateFlow<AskUiState> = combine(stage, recordEngine.progress) { stage, progress ->
        AskUiState(stage = stage, scopeRelation = progress?.session?.relation?.toUi())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AskUiState(),
    )

    /** [음성 질문하기], 홈의 [기억 물어보기]. 바로 듣기 시작한다. */
    fun onOpen() {
        autoCloseJob?.cancel()
        stage.value = AskStage.Listening()
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            updateListening { it.copy(listening = true) }
            var finalText: String? = null
            voiceRecognizer.listen().collect { event ->
                when (event) {
                    is VoiceEvent.Heard -> {
                        updateListening { it.copy(text = event.text.take(MAX_QUESTION_LENGTH)) }
                        if (event.completed) finalText = event.text
                    }
                    VoiceEvent.Failed -> Unit
                }
            }
            updateListening { it.copy(listening = false) }
            finalText?.takeIf { it.isNotBlank() }?.let { ask(it) }
        }
    }

    /** 질문을 손으로 고쳤다. 듣기는 멈춘다. */
    fun onQuestionChange(text: String) {
        listenJob?.cancel()
        updateListening { it.copy(text = text.take(MAX_QUESTION_LENGTH), listening = false) }
    }

    /** 손으로 적은 질문을 보낸다. */
    fun onQuestionSubmit() {
        val listening = stage.value as? AskStage.Listening ?: return
        ask(listening.text)
    }

    /** 예시 질문 칩을 눌렀다. 그 질문을 바로 묻는다. */
    fun onExampleClick(question: String) {
        ask(question)
    }

    /** 답을 화면에 띄웠을 때. 같은 내용을 소리로도 알려 준다. */
    fun onAnswerShown(announcement: String) {
        voiceSpeaker.speak(announcement)
    }

    /** 기록이 없을 때의 [응, 추천해 줘]·[응, 찾아줘], 다른 곳만 있을 때의 [새로 찾기] */
    fun onFreshClick() {
        val answer = stage.value as? AskStage.Answer ?: return
        autoCloseJob?.cancel()
        stage.value = AskStage.Fresh(keywordLabel = answer.keywordLabel)
    }

    /** [목적지 변경]·[여기로 안내]·[여기로 길 안내] */
    fun onGuideClick() {
        val memory = (stage.value as? AskStage.Answer)?.memory ?: return
        onClose()
        _guideRequests.trySend(memory.placeName)
    }

    /** [✕]·[닫기]·[괜찮아요] */
    fun onClose() {
        listenJob?.cancel()
        autoCloseJob?.cancel()
        stage.value = null
    }

    private fun ask(question: String) {
        val text = question.trim()
        if (text.isEmpty()) return
        listenJob?.cancel()
        viewModelScope.launch {
            val progress = recordEngine.progress.first()
            val here = progress?.pendingVisit ?: progress?.savedVisits?.lastOrNull()
            val answer = MemoryAsk.answer(
                question = text,
                diaries = diaryRepository.observeDiaries().first(),
                relation = progress?.session?.relation,
                herePlaceId = here?.place?.id,
            )
            stage.value = answer.toUi()
            // 와본 곳 답은 5초 뒤 저절로 닫힌다 (화면 13).
            if (answer.kind == AskKind.RECALL && answer.type == AskAnswerType.FOUND) {
                autoCloseJob?.cancel()
                autoCloseJob = launch {
                    delay(RECALL_VISIBLE_MILLIS)
                    stage.value = null
                }
            }
        }
    }

    private fun updateListening(transform: (AskStage.Listening) -> AskStage.Listening) {
        stage.update { current -> if (current is AskStage.Listening) transform(current) else current }
    }

    private fun AskAnswer.toUi() = AskStage.Answer(
        question = question,
        kind = kind,
        type = type,
        keywordLabel = keywords.takeIf { it.isNotEmpty() }?.joinToString(separator = " "),
        memory = hit?.toUi(),
        moreCount = moreCount,
        badSkipped = badSkipped,
    )

    private fun Memory.toUi() = AskMemoryUi(
        relation = session.relation.toUi(),
        dateText = session.sessionDate.format(DATE_FORMATTER),
        spokenDateText = session.sessionDate.format(MONTH_DAY_FORMATTER),
        placeName = visit.place.name,
        good = visit.visit.rating == Rating.GOOD,
        memo = visit.visit.memoText?.takeIf { it.isNotBlank() },
    )

    companion object {
        /** 와본 곳 답을 보여 주는 시간 */
        const val RECALL_VISIBLE_MILLIS = 5_000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val MAX_QUESTION_LENGTH = 100
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
        private val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
    }
}
