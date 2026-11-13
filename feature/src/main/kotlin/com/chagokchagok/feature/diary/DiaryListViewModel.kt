package com.chagokchagok.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.toDomain
import com.chagokchagok.feature.common.toUi
import com.chagokchagok.feature.record.home.RelationPickerMode
import com.chagokchagok.feature.record.home.RelationPickerUi
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val recordEngine: RecordEngine,
    seatMonitor: SeatMonitor,
    private val tabMemory: DiaryTabMemory,
    diaryRepository: DiaryRepository,
) : ViewModel() {

    /** 좌석마다 값이 따로 들어오므로 계속 지켜보다가 누를 때의 값을 쓴다. */
    private val seatCount = seatMonitor.occupantCount.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val tab = MutableStateFlow(tabMemory.tab)
    private val relationPicker = MutableStateFlow<RelationPickerUi?>(null)

    private val _recordStarted = Channel<Boolean>(Channel.BUFFERED)

    /** 기록을 시작했을 때 한 번 낸다. 값은 좌석 1명이라 묻지 않고 시작했는지 여부. */
    val recordStarted: Flow<Boolean> = _recordStarted.receiveAsFlow()

    val uiState: StateFlow<DiaryListUiState> = combine(
        diaryRepository.observeDiaries(),
        recordEngine.progress,
        tab,
        relationPicker,
    ) { diaries, progress, tab, picker ->
        val shown = diaries.filter { tab == null || it.session.relation.toUi() == tab }
        DiaryListUiState(
            loaded = true,
            hasDiaries = diaries.isNotEmpty(),
            tab = tab,
            diaries = shown.map { it.toCardUi() },
            pins = pinsOf(shown, slots = slotsOf(diaries)),
            legend = LEGEND_ORDER.mapNotNull { relation ->
                val count = placeCount(diaries.filter { it.session.relation.toUi() == relation })
                if (count > 0) relation to count else null
            },
            recordingRelation = progress?.session?.relation?.toUi(),
            relationPicker = picker,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DiaryListUiState(tab = tabMemory.tab),
    )

    /** 관계 탭. 전체는 null */
    fun onTabClick(relation: RelationUi?) {
        tabMemory.tab = relation
        tab.value = relation
    }

    /** 기록 0건 화면의 [차곡차곡 쌓기 시작하기]. 홈의 [쌓기 시작]과 같게 움직인다. */
    fun onStartRecordClick() {
        val seatCount = seatCount.value
        if (seatCount <= 1) {
            viewModelScope.launch {
                startRecord(relation = RelationUi.SOLO, seatCount = seatCount, autoStarted = true)
            }
        } else {
            relationPicker.value = RelationPickerUi(mode = recommendedMode(seatCount), seatCount = seatCount)
        }
    }

    fun onRelationClick(relation: RelationUi) {
        val picker = relationPicker.value ?: return
        relationPicker.value = null
        viewModelScope.launch {
            startRecord(relation = relation, seatCount = picker.seatCount, autoStarted = false)
        }
    }

    /** [다른 모드 직접 선택] */
    fun onShowAllRelationsClick() {
        relationPicker.update { it?.copy(mode = RelationPickerMode.ALL) }
    }

    /** [추천으로 돌아가기] */
    fun onBackToRecommendedClick() {
        relationPicker.update { it?.copy(mode = recommendedMode(it.seatCount)) }
    }

    /** 관계 팝업의 [✕] */
    fun onRelationPickerClose() {
        relationPicker.value = null
    }

    private suspend fun startRecord(relation: RelationUi, seatCount: Int, autoStarted: Boolean) {
        recordEngine.start(relation = relation.toDomain(), occupantCount = seatCount)
        _recordStarted.send(autoStarted)
    }

    private fun recommendedMode(seatCount: Int): RelationPickerMode =
        if (seatCount == 2) RelationPickerMode.TWO else RelationPickerMode.THREE_OR_MORE

    private fun Diary.toCardUi() = DiaryCardUi(
        sessionId = session.id,
        dateText = session.sessionDate.format(DATE_FORMATTER),
        relation = session.relation.toUi(),
        placeNames = visits.map { it.place.name },
        badCount = visits.count { it.visit.rating == Rating.BAD },
        missingMemoCount = visits.count { it.visit.memoText.isNullOrBlank() },
    )

    /** 장소마다 핀 자리를 하나씩 정한다. 오래된 기록의 장소부터 번호를 준다. */
    private fun slotsOf(diaries: List<Diary>): Map<String, Int> {
        val slots = LinkedHashMap<String, Int>()
        diaries.asReversed().forEach { diary ->
            diary.visits.forEach { saved -> slots.getOrPut(saved.place.id) { slots.size } }
        }
        return slots
    }

    /** 장소마다 가장 최근 기록의 관계 색으로 핀 하나. [diaries]는 최근 것부터다. */
    private fun pinsOf(diaries: List<Diary>, slots: Map<String, Int>): List<DiaryPinUi> {
        val pins = LinkedHashMap<String, DiaryPinUi>()
        diaries.forEach { diary ->
            diary.visits.forEach { saved ->
                pins.getOrPut(saved.place.id) {
                    DiaryPinUi(
                        placeId = saved.place.id,
                        relation = diary.session.relation.toUi(),
                        slot = slots[saved.place.id] ?: 0,
                        sessionId = diary.session.id,
                    )
                }
            }
        }
        return pins.values.toList()
    }

    private fun placeCount(diaries: List<Diary>): Int =
        diaries.flatMap { diary -> diary.visits.map { it.place.id } }.distinct().size

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
        val LEGEND_ORDER = listOf(RelationUi.COUPLE, RelationUi.FAMILY, RelationUi.FRIEND, RelationUi.SOLO)
    }
}
