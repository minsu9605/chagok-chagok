package com.chagokchagok.feature.fake

import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.record.RecordEngine
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * [임시 구현] core:domain 인터페이스에 메모리 구현을 이어 준다.
 * core:database와 core:pleos가 실제 구현을 제공하면 이 모듈을 지운다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class FakeBindingsModule {
    @Binds
    abstract fun bindRecordEngine(store: InMemoryRecordStore): RecordEngine

    @Binds
    abstract fun bindDiaryRepository(store: InMemoryRecordStore): DiaryRepository

    @Binds
    abstract fun bindSeatMonitor(monitor: FakeSeatMonitor): SeatMonitor
}
