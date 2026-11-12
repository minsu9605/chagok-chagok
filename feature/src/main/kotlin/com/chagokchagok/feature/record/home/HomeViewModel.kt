package com.chagokchagok.feature.record.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chagokchagok.feature.fake.FakeLastRecord
import com.chagokchagok.feature.fake.FakeRecordStore
import com.chagokchagok.feature.fake.FakeSession
import com.chagokchagok.feature.fake.toRelationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // [임시 가짜 구현] core:domain 연결 후 core:domain 인터페이스로 바꾼다.
    fakeRecordStore: FakeRecordStore,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        fakeRecordStore.seatCount,
        fakeRecordStore.activeSession,
        fakeRecordStore.lastRecord,
    ) { seatCount, activeSession, lastRecord ->
        HomeUiState(
            dateText = LocalDate.now().format(TODAY_FORMATTER),
            seatCount = seatCount,
            lastRecord = lastRecord?.toUi(),
            recording = activeSession?.toUi(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(),
    )

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
        val TODAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
        val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
    }
}
