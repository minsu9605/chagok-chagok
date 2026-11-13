package com.chagokchagok.core.pleos.di

import com.chagokchagok.core.domain.navi.NaviController
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.core.pleos.navi.PleosNaviController
import com.chagokchagok.core.pleos.vehicle.PleosSeatMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** core:domain의 차량 인터페이스에 Pleos SDK 구현을 이어 준다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class PleosModule {
    @Binds
    abstract fun bindSeatMonitor(monitor: PleosSeatMonitor): SeatMonitor

    @Binds
    abstract fun bindNaviController(controller: PleosNaviController): NaviController
}
