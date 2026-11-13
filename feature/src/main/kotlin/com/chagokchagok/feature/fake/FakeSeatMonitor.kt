package com.chagokchagok.feature.fake

import com.chagokchagok.core.domain.vehicle.SeatMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [임시 구현] 탑승 인원을 손으로 바꾸는 가짜 좌석 감지. core:pleos 좌석 감지 연결 후 삭제.
 * 처음 값은 2명이다.
 */
@Singleton
class FakeSeatMonitor @Inject constructor() : SeatMonitor {
    private val count = MutableStateFlow(INITIAL_SEAT_COUNT)

    override val occupantCount: Flow<Int> = count.asStateFlow()

    /** 탑승 인원을 1 → 2 → 3 → 1 순서로 바꾼다. 인원별 화면을 확인하려고 둔 시험용 기능이다. */
    fun cycle() {
        count.value = if (count.value >= MAX_TEST_SEAT_COUNT) 1 else count.value + 1
    }

    private companion object {
        const val INITIAL_SEAT_COUNT = 2
        const val MAX_TEST_SEAT_COUNT = 3
    }
}
