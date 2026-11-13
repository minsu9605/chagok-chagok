package com.chagokchagok.feature.fake

import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.record.RecordEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * [임시 구현] core:domain의 기록·일기 인터페이스에 메모리 구현을 이어 준다.
 * core:database가 실제 구현을 제공하면 이 모듈을 지운다. (탑승 인원은 core:pleos가 제공한다.)
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class FakeBindingsModule {
    @Binds
    abstract fun bindRecordEngine(store: InMemoryRecordStore): RecordEngine

    @Binds
    abstract fun bindDiaryRepository(store: InMemoryRecordStore): DiaryRepository
}
