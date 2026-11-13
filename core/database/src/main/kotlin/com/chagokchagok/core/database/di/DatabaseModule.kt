package com.chagokchagok.core.database.di

import android.content.Context
import androidx.room.Room
import com.chagokchagok.core.database.ChagokDatabase
import com.chagokchagok.core.database.RoomRecordStore
import com.chagokchagok.core.domain.diary.DiaryRepository
import com.chagokchagok.core.domain.record.RecordEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** core:domain의 기록·일기 인터페이스에 Room 구현을 이어 준다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseModule {
    @Binds
    abstract fun bindRecordEngine(store: RoomRecordStore): RecordEngine

    @Binds
    abstract fun bindDiaryRepository(store: RoomRecordStore): DiaryRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): ChagokDatabase =
            Room.databaseBuilder(context, ChagokDatabase::class.java, ChagokDatabase.NAME).build()
    }
}
