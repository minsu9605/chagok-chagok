package com.chagokchagok.core.domain.record

import com.chagokchagok.core.domain.model.DriveSession
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.model.RecordProgress
import com.chagokchagok.core.domain.model.Relation
import com.chagokchagok.core.domain.model.Visit
import kotlinx.coroutines.flow.Flow

/**
 * 기록 엔진: 시작 → 도착 → 평가(→ 한마디) → 종료.
 * 기록은 한 번에 하나만 진행한다.
 */
interface RecordEngine {
    /** 진행 중인 기록. 기록 중이 아니면 null을 낸다. */
    val progress: Flow<RecordProgress?>

    /**
     * 기록을 시작한다. 이미 기록 중이면 새로 만들지 않고 진행 중인 기록을 돌려준다.
     *
     * @param occupantCount 시작할 때의 탑승 인원
     */
    suspend fun start(relation: Relation, occupantCount: Int): DriveSession

    /**
     * 장소에 도착했다. 평가 전 방문을 만든다.
     * 평가하지 않은 이전 방문이 남아 있으면 그 방문은 기록하지 않고 지운다.
     *
     * @return 만든 방문. 기록 중이 아니면 null
     */
    suspend fun arrive(placeName: String, address: String?): Visit?

    /**
     * 도착한 곳에서 시동을 다시 켰다. 평가 전 방문에 복귀 시각을 적는다.
     * 평가 전 방문이 없거나 이미 적었으면 아무것도 하지 않는다.
     */
    suspend fun markReturned()

    /** 평가 전 방문을 좋았어요·별로예요로 저장한다. 없는 방문이면 아무것도 하지 않는다. */
    suspend fun rate(visitId: String, rating: Rating)

    /** 저장된 방문에 한마디를 남긴다. 없는 방문이면 아무것도 하지 않는다. */
    suspend fun saveMemo(visitId: String, text: String)

    /** 평가하지 않고 닫았다. 평가 전 방문을 지운다. */
    suspend fun discardPending(visitId: String)

    /**
     * 끝내지 않은 기록이 마지막 갱신 뒤 3일 넘게 그대로면, 그때까지 저장된 방문으로 마감한다.
     * 끝난 시각은 마지막 갱신 시각으로 적는다. 앱이 뜰 때 한 번 부른다.
     *
     * @return 마감한 기록의 id. 마감할 것이 없었으면 null
     */
    suspend fun closeStaleRecord(): String?

    /**
     * 기록을 끝낸다. 평가 전 방문이 남아 있으면 지운다.
     *
     * @return 끝난 기록의 id. 기록 중이 아니었으면 null
     */
    suspend fun end(): String?
}
