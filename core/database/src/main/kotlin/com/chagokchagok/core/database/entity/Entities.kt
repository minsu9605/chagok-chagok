package com.chagokchagok.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * 테이블과 컬럼 이름은 기획 문서 "DB 설계" 2장을 따른다. 지금 쓰는 컬럼만 만들었다.
 * 공통: 시각은 epoch millis, 정해진 값(enum)은 이름 문자열, PK는 UUID 문자열.
 */

/** 일기 1권. */
@Entity(tableName = "drive_session")
data class DriveSessionEntity(
    @PrimaryKey @ColumnInfo(name = "session_id") val sessionId: String,
    /** 일기 날짜 `YYYY-MM-DD` (시작 시각 기준) */
    @ColumnInfo(name = "session_date") val sessionDate: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    /** 진행 중이면 null */
    @ColumnInfo(name = "ended_at") val endedAt: Long?,
    /** `ACTIVE` · `ENDED_MANUAL` */
    @ColumnInfo(name = "status") val status: String,
    /** `SOLO` · `COUPLE` · `FRIENDS` · `FAMILY` */
    @ColumnInfo(name = "relation") val relation: String,
    @ColumnInfo(name = "initial_occupant_count") val initialOccupantCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    /** 세션이나 그 방문이 바뀔 때마다 갱신한다 */
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** 장소 마스터. */
@Entity(tableName = "place")
data class PlaceEntity(
    @PrimaryKey @ColumnInfo(name = "place_id") val placeId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** 방문 카드 1장. 일기를 지우면 함께 지워진다. */
@Entity(
    tableName = "visit",
    foreignKeys = [
        ForeignKey(
            entity = DriveSessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["place_id"],
            childColumns = ["place_id"],
        ),
    ],
    indices = [Index("session_id"), Index("place_id")],
)
data class VisitEntity(
    @PrimaryKey @ColumnInfo(name = "visit_id") val visitId: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "place_id") val placeId: String,
    /** 그날 방문 순서. 1부터 */
    @ColumnInfo(name = "seq") val seq: Int,
    @ColumnInfo(name = "arrived_at") val arrivedAt: Long,
    /** 시동을 다시 켠 시각. 아직이면 null */
    @ColumnInfo(name = "returned_at") val returnedAt: Long?,
    /** `PENDING` 도착함·응답 전 · `SAVED` 저장됨 */
    @ColumnInfo(name = "status") val status: String,
    /** `GOOD` · `BAD`. `SAVED`면 반드시 있다 */
    @ColumnInfo(name = "rating") val rating: String?,
    @ColumnInfo(name = "memo_text") val memoText: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** 방문과 그 장소. */
data class VisitWithPlaceEntity(
    @Embedded val visit: VisitEntity,
    @Relation(parentColumn = "place_id", entityColumn = "place_id")
    val place: PlaceEntity,
)
