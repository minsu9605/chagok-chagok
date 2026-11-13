package com.chagokchagok.feature.fake

import com.chagokchagok.feature.common.RelationUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/*
 * ─────────────────────────────────────────────────────────────────────────────
 * [임시 가짜 구현] 이 `fake` 패키지는 통째로 지울 코드입니다.
 *
 * - 왜 있나: 화면(feature)은 core:domain의 모델·인터페이스로 동작해야 하는데,
 *   core:domain이 아직 비어 있어 화면을 먼저 만들 수 있도록 메모리 안에서만 동작하는 가짜를 둡니다.
 * - 언제 지우나: core:domain에 모델·인터페이스가 올라오면, 이 패키지를 지우고
 *   각 ViewModel이 core:domain 인터페이스를 주입받도록 바꿉니다.
 * - 주의: 여기의 이름과 모양은 core:domain의 설계가 아닙니다. 참고하거나 맞추지 않아도 됩니다.
 *   저장하지 않으므로 앱을 다시 켜면 처음 상태로 돌아갑니다.
 * - 이 패키지를 쓰는 곳: 각 화면의 ViewModel뿐입니다. 화면(Composable)과 UiState는 쓰지 않습니다.
 * ─────────────────────────────────────────────────────────────────────────────
 */

/** [임시 가짜 구현] 관계. core:domain 연결 후 삭제. */
enum class FakeRelation { SOLO, COUPLE, FRIEND, FAMILY }

/** [임시 가짜 구현] 기록 중인 하루. core:domain 연결 후 삭제. */
data class FakeSession(
    val relation: FakeRelation,
    val seatCount: Int,
    val startedAt: LocalDateTime,
    val placeNames: List<String> = emptyList(),
)

/** [임시 가짜 구현] 마지막으로 끝난 기록의 요약. core:domain 연결 후 삭제. */
data class FakeLastRecord(
    val date: LocalDate,
    val relation: FakeRelation,
    val placeCount: Int,
)

/** [임시 가짜 구현] 지금까지 쌓인 일기와 핀의 수. core:domain 연결 후 삭제. */
data class FakeDiarySummary(
    val diaryCount: Int,
    val pinCount: Int,
)

/**
 * [임시 가짜 구현] 기록 상태를 메모리에만 들고 있는 저장소. core:domain 연결 후 삭제.
 * 처음 값은 좌석 2명에 쌓인 기록이 하나도 없는 상태(지난 기록 없음, 일기 0권)이다.
 */
@Singleton
class FakeRecordStore @Inject constructor() {
    private val _seatCount = MutableStateFlow(2)
    val seatCount: StateFlow<Int> = _seatCount.asStateFlow()

    private val _activeSession = MutableStateFlow<FakeSession?>(null)
    val activeSession: StateFlow<FakeSession?> = _activeSession.asStateFlow()

    private val _lastRecord = MutableStateFlow<FakeLastRecord?>(null)
    val lastRecord: StateFlow<FakeLastRecord?> = _lastRecord.asStateFlow()

    /** [임시 가짜 구현] 아직 쌓인 일기가 없는 상태. */
    private val _diarySummary = MutableStateFlow(FakeDiarySummary(diaryCount = 0, pinCount = 0))
    val diarySummary: StateFlow<FakeDiarySummary> = _diarySummary.asStateFlow()

    /** [임시 가짜 구현] 고른 관계로 기록을 시작한다. 이미 기록 중이면 아무것도 하지 않는다. */
    fun startSession(relation: FakeRelation) {
        if (_activeSession.value != null) return
        _activeSession.value = FakeSession(
            relation = relation,
            seatCount = _seatCount.value,
            startedAt = LocalDateTime.now(),
        )
    }

    /**
     * [임시 가짜 구현] 탑승 인원을 1 → 2 → 3 → 1 순서로 바꾼다.
     * 실제 좌석 감지(core:pleos)가 없어 인원별 화면을 확인하려고 둔 시험용 기능이다.
     */
    fun cycleSeatCount() {
        _seatCount.value = if (_seatCount.value >= MAX_TEST_SEAT_COUNT) 1 else _seatCount.value + 1
    }

    private companion object {
        const val MAX_TEST_SEAT_COUNT = 3
    }
}

/** [임시 가짜 구현] 가짜 관계를 화면용 관계로 바꾼다. core:domain 연결 후 삭제. */
internal fun FakeRelation.toRelationUi(): RelationUi = when (this) {
    FakeRelation.SOLO -> RelationUi.SOLO
    FakeRelation.COUPLE -> RelationUi.COUPLE
    FakeRelation.FRIEND -> RelationUi.FRIEND
    FakeRelation.FAMILY -> RelationUi.FAMILY
}

/** [임시 가짜 구현] 화면용 관계를 가짜 관계로 바꾼다. core:domain 연결 후 삭제. */
internal fun RelationUi.toFakeRelation(): FakeRelation = when (this) {
    RelationUi.SOLO -> FakeRelation.SOLO
    RelationUi.COUPLE -> FakeRelation.COUPLE
    RelationUi.FRIEND -> FakeRelation.FRIEND
    RelationUi.FAMILY -> FakeRelation.FAMILY
}
