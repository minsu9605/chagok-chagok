package com.chagokchagok.core.domain.diary

import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.DiarySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 끝난 기록(일기)을 읽는다. */
interface DiaryRepository {
    /** 가장 최근에 끝난 일기. 방문이 하나도 없는 일기는 건너뛴다. 없으면 null을 낸다. */
    fun observeLastDiary(): Flow<Diary?>

    /** 지금까지 쌓인 일기와 핀의 수. */
    fun observeSummary(): Flow<DiarySummary>

    /** 그 날짜의 끝난 일기들에 저장된 방문 수. */
    fun observeVisitCountOn(date: LocalDate): Flow<Int>

    /** 일기 1권. 없으면 null */
    suspend fun getDiary(sessionId: String): Diary?

    /** 끝난 일기의 방문에 한마디를 남기거나 고친다. 없는 방문이면 아무것도 하지 않는다. */
    suspend fun saveDiaryMemo(visitId: String, text: String)
}
