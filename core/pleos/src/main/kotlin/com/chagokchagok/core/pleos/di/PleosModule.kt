package com.chagokchagok.core.pleos.di

import com.chagokchagok.core.domain.navi.NaviController
import com.chagokchagok.core.domain.vehicle.IgnitionMonitor
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import com.chagokchagok.core.domain.voice.VoiceRecognizer
import com.chagokchagok.core.domain.voice.VoiceSpeaker
import com.chagokchagok.core.pleos.navi.PleosNaviController
import com.chagokchagok.core.pleos.vehicle.PleosSeatMonitor
import com.chagokchagok.core.pleos.vehicle.ScreenOnIgnitionMonitor
import com.chagokchagok.core.pleos.voice.PleosVoiceRecognizer
import com.chagokchagok.core.pleos.voice.PleosVoiceSpeaker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** core:domain의 차량·내비·음성 인터페이스에 Pleos SDK 구현을 이어 준다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class PleosModule {
    @Binds
    abstract fun bindSeatMonitor(monitor: PleosSeatMonitor): SeatMonitor

    @Binds
    abstract fun bindNaviController(controller: PleosNaviController): NaviController

    @Binds
    abstract fun bindIgnitionMonitor(monitor: ScreenOnIgnitionMonitor): IgnitionMonitor

    @Binds
    abstract fun bindVoiceRecognizer(recognizer: PleosVoiceRecognizer): VoiceRecognizer

    @Binds
    abstract fun bindVoiceSpeaker(speaker: PleosVoiceSpeaker): VoiceSpeaker
}
