package com.chagokchagok.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chagokchagok.core.database.dao.RecordDao
import com.chagokchagok.core.database.entity.DriveSessionEntity
import com.chagokchagok.core.database.entity.PlaceEntity
import com.chagokchagok.core.database.entity.VisitEntity

/** 차 안에 두는 기록 DB. 모든 기록을 여기에만 저장한다. */
@Database(
    entities = [DriveSessionEntity::class, PlaceEntity::class, VisitEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ChagokDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        const val NAME = "chagok.db"
    }
}
