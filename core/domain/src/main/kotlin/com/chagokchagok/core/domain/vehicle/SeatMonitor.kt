package com.chagokchagok.core.domain.vehicle

import kotlinx.coroutines.flow.Flow

/** 차량의 탑승 인원을 알려 준다. */
interface SeatMonitor {
    /** 현재 탑승 인원. 운전자를 포함한다. */
    val occupantCount: Flow<Int>
}
