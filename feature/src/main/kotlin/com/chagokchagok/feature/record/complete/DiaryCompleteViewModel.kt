package com.chagokchagok.feature.record.complete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.feature.common.toUi
import com.chagokchagok.feature.navigation.DiaryCompleteDestination
import com.chagokchagok.feature.record.memory.MemoryPromptUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiaryCompleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
) : ViewModel() {
    private val sessionId = savedStateHandle.toRoute<DiaryCompleteDestination>().sessionId

    private val _uiState = MutableStateFlow(DiaryCompleteUiState())
    val uiState: StateFlow<DiaryCompleteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { reload() }
    }

    /** [지금 한마디 추가하기] */
    fun onAddMemoClick(visitId: String) {
        val visit = _uiState.value.visits.firstOrNull { it.visitId == visitId } ?: return
        _uiState.update {
            it.copy(
                memoEditing = MemoryPromptUi.Memo(
                    visitId = visit.visitId,
                    placeName = visit.placeName,
                    good = visit.good,
                    text = "",
                ),
            )
        }
    }

    fun onMemoTextChange(text: String) {
        _uiState.update { state ->
            state.copy(memoEditing = state.memoEditing?.copy(text = text.take(MAX_MEMO_LENGTH)))
        }
    }

    /** 한마디 추가 팝업의 [저장] */
    fun onMemoSaveClick() {
        val editing = _uiState.value.memoEditing ?: return
        val text = editing.text.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            diaryRepository.saveDiaryMemo(editing.visitId, text)
            reload()
        }
    }

    /** 한마디 추가 팝업의 [다음에 말하기] */
    fun onMemoLaterClick() {
        _uiState.update { it.copy(memoEditing = null) }
    }

    private suspend fun reload() {
        val diary = diaryRepository.getDiary(sessionId)
        _uiState.value = diary?.toUi() ?: DiaryCompleteUiState(loaded = true)
    }

    private fun Diary.toUi() = DiaryCompleteUiState(
        loaded = true,
        relation = session.relation.toUi(),
        dateText = session.sessionDate.format(DATE_FORMATTER),
        visits = visits.map { saved ->
            CompleteVisitUi(
                visitId = saved.visit.id,
                placeName = saved.place.name,
                good = saved.visit.rating == Rating.GOOD,
                arrivedAtText = saved.visit.arrivedAt.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER),
                stayMinutes = saved.visit.returnedAt?.let { Duration.between(saved.visit.arrivedAt, it).toMinutes() },
                memo = saved.visit.memoText,
            )
        },
    )

    private companion object {
        const val MAX_MEMO_LENGTH = 100
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
    }
}
