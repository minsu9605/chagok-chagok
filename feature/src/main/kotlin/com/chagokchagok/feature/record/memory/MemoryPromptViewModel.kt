package com.chagokchagok.feature.record.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.model.RecordProgress
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.IgnitionMonitor
import com.chagokchagok.feature.common.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * 기억 남기기 팝업의 흐름: 시동을 다시 켬 → 좋았어요·별로예요(10초) → 한마디 → 저장 완료(2초).
 */
@HiltViewModel
class MemoryPromptViewModel @Inject constructor(
    private val recordEngine: RecordEngine,
    ignitionMonitor: IgnitionMonitor,
) : ViewModel() {

    /** 평가를 마친 뒤의 단계(한마디, 저장 완료). 평가 단계는 기록 엔진의 상태에서 만든다. */
    private val afterRating = MutableStateFlow<MemoryPromptUi?>(null)
    private var rateTimeoutJob: Job? = null
    private var savedDraft: SavedDraft? = null

    val uiState: StateFlow<MemoryPromptUi?> = combine(recordEngine.progress, afterRating) { progress, after ->
        after ?: progress?.toRateUi()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    init {
        // 시동을 다시 켜면 도착해 있던 곳의 평가를 물을 차례가 된다.
        viewModelScope.launch {
            ignitionMonitor.restarted.collect { recordEngine.markReturned() }
        }
        // 평가 팝업이 뜨면 10초를 센다. 답이 없으면 그 방문은 기록하지 않는다.
        viewModelScope.launch {
            recordEngine.progress
                .map { it?.promptingVisitId() }
                .distinctUntilChanged()
                .collect { visitId ->
                    rateTimeoutJob?.cancel()
                    if (visitId != null) {
                        rateTimeoutJob = launch {
                            delay(RATE_TIMEOUT_MILLIS)
                            recordEngine.discardPending(visitId)
                        }
                    }
                }
        }
    }

    /** [좋았어요] · [별로예요] */
    fun onRateClick(good: Boolean) {
        val rate = uiState.value as? MemoryPromptUi.Rate ?: return
        rateTimeoutJob?.cancel()
        afterRating.value = MemoryPromptUi.Memo(
            visitId = rate.visitId,
            placeName = rate.placeName,
            good = good,
            text = "",
        )
        savedDraft = SavedDraft(rate = rate, good = good)
        viewModelScope.launch {
            recordEngine.rate(rate.visitId, if (good) Rating.GOOD else Rating.BAD)
        }
    }

    /** 평가 팝업의 [✕]. 그 방문은 기록하지 않는다. */
    fun onRateClose() {
        val rate = uiState.value as? MemoryPromptUi.Rate ?: return
        rateTimeoutJob?.cancel()
        viewModelScope.launch { recordEngine.discardPending(rate.visitId) }
    }

    fun onMemoTextChange(text: String) {
        afterRating.update { current ->
            if (current is MemoryPromptUi.Memo) current.copy(text = text.take(MAX_MEMO_LENGTH)) else current
        }
    }

    /** 한마디 [저장] */
    fun onMemoSaveClick() {
        val memo = afterRating.value as? MemoryPromptUi.Memo ?: return
        val text = memo.text.trim()
        if (text.isEmpty()) return
        viewModelScope.launch { recordEngine.saveMemo(memo.visitId, text) }
        showSaved(memo = text)
    }

    /** [다음에 말하기]. 평가만 저장된 채로 끝낸다. */
    fun onMemoLaterClick() {
        if (afterRating.value !is MemoryPromptUi.Memo) return
        showSaved(memo = null)
    }


    private fun showSaved(memo: String?) {
        val draft = savedDraft ?: return
        afterRating.value = MemoryPromptUi.Saved(
            relation = draft.rate.relation,
            placeName = draft.rate.placeName,
            arrivedAtText = draft.rate.arrivedDateTimeText,
            good = draft.good,
            memo = memo,
        )
        viewModelScope.launch {
            delay(SAVED_VISIBLE_MILLIS)
            afterRating.value = null
        }
    }

    private fun RecordProgress.promptingVisitId(): String? =
        pendingVisit?.visit?.takeIf { it.returnedAt != null }?.id

    private fun RecordProgress.toRateUi(): MemoryPromptUi.Rate? {
        val pending = pendingVisit ?: return null
        val returnedAt = pending.visit.returnedAt ?: return null
        val arrivedAt = pending.visit.arrivedAt.atZone(ZoneId.systemDefault())
        return MemoryPromptUi.Rate(
            visitId = pending.visit.id,
            relation = session.relation.toUi(),
            placeName = pending.place.name,
            arrivedAtText = arrivedAt.format(TIME_FORMATTER),
            stayMinutes = Duration.between(pending.visit.arrivedAt, returnedAt).toMinutes(),
            arrivedDateTimeText = arrivedAt.format(DATE_TIME_FORMATTER),
        )
    }

    /** 저장 완료 화면에 쓸 값을 평가 단계에서 잡아 둔다. */
    private data class SavedDraft(val rate: MemoryPromptUi.Rate, val good: Boolean)

    companion object {
        /** 평가를 기다리는 시간 */
        const val RATE_TIMEOUT_MILLIS = 10_000L

        /** 저장 완료 화면을 보여 주는 시간 */
        const val SAVED_VISIBLE_MILLIS = 2_000L
        private const val MAX_MEMO_LENGTH = 100
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
        private val DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN)
    }
}
