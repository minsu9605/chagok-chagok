package com.chagokchagok.core.domain.diary

import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.DiarySummary
import kotlinx.coroutines.flow.Flow

/** 끝난 기록(일기)을 읽는다. */
interface DiaryRepository {
    /** 가장 최근에 끝난 일기. 방문이 하나도 없는 일기는 건너뛴다. 없으면 null을 낸다. */
    fun observeLastDiary(): Flow<Diary?>

    /** 지금까지 쌓인 일기와 핀의 수. */
    fun observeSummary(): Flow<DiarySummary>

    /** 일기 1권. 없으면 null */
    suspend fun getDiary(sessionId: String): Diary?
}
