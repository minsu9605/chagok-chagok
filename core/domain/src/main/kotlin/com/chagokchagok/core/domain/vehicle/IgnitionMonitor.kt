package com.chagokchagok.core.domain.vehicle

import kotlinx.coroutines.flow.Flow

/** 시동을 다시 켠 순간을 알려 준다. */
interface IgnitionMonitor {
    /** 시동을 다시 켤 때마다 한 번 낸다. */
    val restarted: Flow<Unit>
}
