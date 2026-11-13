package com.chagokchagok.feature.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.chagokchagok.core.domain.ask.Memory
import com.chagokchagok.core.domain.course.CourseCase
import com.chagokchagok.core.domain.course.CoursePlan
import com.chagokchagok.core.domain.course.MemoryCourse
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Relation
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.core.domain.voice.VoiceEvent
import com.chagokchagok.core.domain.voice.VoiceRecognizer
import com.chagokchagok.feature.common.toUi
import com.chagokchagok.feature.navigation.CourseDestination
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

/**
 * 코스 짜기의 흐름: 첫 마디 → 기록 상황(Case A·B·C)에 따라 묻고 답하기 → 코스 카드 → 출발.
 * 내 기억은 실제 기록에서 찾는다. 새 장소는 지도 서비스를 붙이기 전이라 예시 장소를 보여 준다.
 */
@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val recordEngine: RecordEngine,
    private val voiceRecognizer: VoiceRecognizer,
    seatMonitor: SeatMonitor,
) : ViewModel() {
    private val relation: Relation = Relation.valueOf(savedStateHandle.toRoute<CourseDestination>().relation)

    private val state = MutableStateFlow(CourseUiState(relation = relation.toUi()))
    private var plan: CoursePlan? = null
    private var listenJob: Job? = null

    /** 좌석마다 값이 따로 들어오므로 계속 지켜보다가 출발할 때의 값을 쓴다. */
    private val seatCount = seatMonitor.occupantCount.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _departed = Channel<Boolean>(Channel.BUFFERED)

    /** 출발했을 때 한 번 낸다. 값은 새 기록을 시작했는지 여부 (하던 기록에 이어서 쌓으면 false). */
    val departed: Flow<Boolean> = _departed.receiveAsFlow()

    val uiState: StateFlow<CourseUiState> = combine(state, recordEngine.progress) { state, progress ->
        state.copy(recording = progress != null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = state.value,
    )

    init {
        startListening()
    }

    // ── 시작 (화면 36) ──────────────────────────────────────────────────

    /** 첫 마디를 손으로 고쳤다. 듣기는 멈춘다. */
    fun onTextChange(text: String) {
        listenJob?.cancel()
        state.update { it.copy(heardText = text.take(MAX_UTTERANCE_LENGTH), listening = false) }
    }

    /** 손으로 적은 첫 마디를 보낸다. */
    fun onTextSubmit() {
        begin(state.value.heardText)
    }

    /** 추천 칩. 누르면 그 말을 한 것처럼 시작한다. */
    fun onQuickStartClick(utterance: String) {
        begin(utterance)
    }

    /** [다시 말하기]·[다른 지역으로 바꿀래] */
    fun onRestartClick() {
        plan = null
        state.update { CourseUiState(relation = it.relation) }
        startListening()
    }

    // ── 묻고 답하기 (화면 37~41) ────────────────────────────────────────

    /** Case A의 [식사부터]·[카페부터]. 업종 데이터가 없어 무엇을 골라도 같은 순서로 코스를 만든다. */
    fun onFirstPick() {
        val plan = plan ?: return
        showCard((plan.matched + plan.others).take(MAX_COURSE_SIZE).map { it.toItem() })
    }

    /**
     * Case B의 선택.
     *
     * @param keepMemory 권한 기억을 코스에 넣으면 true, 전부 새로운 곳으로 하면 false
     */
    fun onKeepPick(keepMemory: Boolean) {
        val step = state.value.step as? CourseStep.CaseB ?: return
        keptMemory = step.memory.takeIf { keepMemory }
        state.update {
            it.copy(
                step = CourseStep.NewCandidates(utterance = step.utterance, candidates = SAMPLE_NEW_PLACES),
                questionCount = it.questionCount + 1,
            )
        }
    }

    /** 새 후보 가운데 하나를 골랐다. */
    fun onCandidatePick(candidate: CourseItemUi) {
        showCard(listOfNotNull(keptMemory, candidate))
    }

    /** Case C의 [응, 새로 제안해 줘] */
    fun onSuggestNewClick() {
        val step = state.value.step as? CourseStep.CaseC ?: return
        state.update {
            it.copy(
                step = CourseStep.Themes(utterance = step.utterance, themes = SAMPLE_THEMES),
                questionCount = it.questionCount + 1,
            )
        }
    }

    fun onThemePick(theme: CourseThemeUi) {
        showCard(theme.items)
    }

    // ── 코스 카드 (화면 42, 42-1, 43) ───────────────────────────────────

    /** [변경]. 코스에 없는 내 기억과 새 장소를 후보로 보여 준다. */
    fun onSwapClick(index: Int) {
        val current = state.value.items.map { it.name }
        val memories = plan?.let { (it.matched + it.others).map { memory -> memory.toItem() } }.orEmpty()
        val candidates = (memories + SAMPLE_NEW_PLACES).filter { it.name !in current }.take(MAX_SWAP_CANDIDATES)
        state.update { it.copy(swap = CourseSwapUi(index = index, candidates = candidates)) }
    }

    /** 후보의 [이 곳으로]. 묻지 않고 바로 바꾼다. */
    fun onSwapPick(candidate: CourseItemUi) {
        state.update { current ->
            val swap = current.swap ?: return@update current
            current.copy(
                items = current.items.mapIndexed { index, item -> if (index == swap.index) candidate else item },
                swap = null,
                swappedNote = swap.index to candidate.name,
            )
        }
    }

    /** [그대로 두기] */
    fun onSwapClose() {
        state.update { it.copy(swap = null) }
    }

    /**
     * [이 코스로 출발하기]. 이미 기록 중이면 그 기록에 이어서 쌓고, 아니면 고른 관계로 기록을 켠다.
     * 장소에 좌표가 없어서 코스를 내비에 넣지는 못한다. 기록을 켜고 주행 화면으로 가는 데까지만 한다.
     */
    fun onDepartClick() {
        viewModelScope.launch {
            if (recordEngine.progress.first() != null) {
                _departed.send(false)
            } else {
                recordEngine.start(relation = relation, occupantCount = seatCount.value)
                _departed.send(true)
            }
        }
    }

    // ── 공통 ────────────────────────────────────────────────────────────

    /** Case B에서 코스에 넣기로 한 기억. 넣지 않기로 했으면 null */
    private var keptMemory: CourseItemUi? = null

    /** 첫 마디로 기록 상황을 가려 다음 단계를 정한다. */
    private fun begin(utterance: String) {
        val text = utterance.trim()
        if (text.isEmpty()) return
        listenJob?.cancel()
        viewModelScope.launch {
            val plan = MemoryCourse.plan(
                utterance = text,
                diaries = diaryRepository.observeDiaries().first(),
                relation = relation,
            )
            this@CourseViewModel.plan = plan
            val label = plan.keywords.takeIf { it.isNotEmpty() }?.joinToString(separator = " ")
            val step = when (plan.case) {
                CourseCase.A -> CourseStep.CaseA(utterance = text, keywordLabel = label, memoryCount = plan.matched.size)
                CourseCase.B -> CourseStep.CaseB(
                    utterance = text,
                    keywordLabel = label.orEmpty(),
                    memory = plan.others.first().toItem(),
                )
                CourseCase.C -> CourseStep.CaseC(utterance = text)
            }
            state.update { it.copy(step = step, listening = false, questionCount = 1) }
        }
    }

    private fun showCard(items: List<CourseItemUi>) {
        state.update { it.copy(step = CourseStep.Card, items = items, swap = null, swappedNote = null) }
    }

    /** 첫 마디를 마이크로 듣는다. 말이 끝나면 저절로 다음 단계로 간다. 듣지 못하면 글자 입력으로 남긴다. */
    private fun startListening() {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            state.update { it.copy(listening = true) }
            var finalText: String? = null
            voiceRecognizer.listen().collect { event ->
                when (event) {
                    is VoiceEvent.Heard -> {
                        state.update { it.copy(heardText = event.text.take(MAX_UTTERANCE_LENGTH)) }
                        if (event.completed) finalText = event.text
                    }
                    VoiceEvent.Failed -> Unit
                }
            }
            state.update { it.copy(listening = false) }
            finalText?.takeIf { it.isNotBlank() }?.let { begin(it) }
        }
    }

    private fun Memory.toItem() = CourseItemUi(
        name = visit.place.name,
        fromMemory = true,
        dateText = session.sessionDate.format(DATE_FORMATTER),
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MAX_UTTERANCE_LENGTH = 100

        /** 코스는 최대 3곳: 내비 경유지 2 + 목적지 1 */
        const val MAX_COURSE_SIZE = 3
        const val MAX_SWAP_CANDIDATES = 3
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)

        /*
         * [예시 데이터] 새 장소 추천은 지도 서비스(카카오 로컬)를 붙여야 실제로 찾을 수 있다.
         * 그때까지는 화면 설계 문서의 예시 장소를 그대로 보여 준다. 실제 검색 결과가 아니다.
         */
        val SAMPLE_NEW_PLACES = listOf(
            CourseItemUi(name = "팔공 브리즈", fromMemory = false, category = "카페", distanceText = "약 2km"),
            CourseItemUi(name = "마운틴 뷰 카페", fromMemory = false, category = "카페", distanceText = "약 4km"),
        )
        private val SAMPLE_MEAL = CourseItemUi(name = "환호 물회", fromMemory = false, category = "식당")
        private val SAMPLE_CAFE = CourseItemUi(name = "영일대 바다 카페", fromMemory = false, category = "카페")
        val SAMPLE_THEMES = listOf(
            CourseThemeUi(title = "물회집부터", flow = "식사 → 바다 카페", items = listOf(SAMPLE_MEAL, SAMPLE_CAFE)),
            CourseThemeUi(title = "베이커리 카페부터", flow = "카페 → 식사", items = listOf(SAMPLE_CAFE, SAMPLE_MEAL)),
        )
    }
}
