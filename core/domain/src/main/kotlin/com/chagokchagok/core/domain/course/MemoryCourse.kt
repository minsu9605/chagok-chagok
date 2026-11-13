package com.chagokchagok.core.domain.course

import com.chagokchagok.core.domain.ask.Memory
import com.chagokchagok.core.domain.ask.MemoryAsk
import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.model.Relation

/** 코스를 짤 때의 기록 상황. */
enum class CourseCase {
    /** 원하는 좋았던 기록이 있다 */
    A,

    /** 원하는 기록은 없지만 다른 좋았던 기록이 있다 */
    B,

    /** 좋았던 기록이 하나도 없다 */
    C,
}

/**
 * 첫 마디로 정한 코스의 재료.
 *
 * @property keywords 첫 마디에서 뽑은 찾는 말. 없으면 비어 있다
 * @property matched 찾는 말에 맞는 좋았던 기억 (장소마다 하나, 최근 것부터). 찾는 말이 없으면 좋았던 기억 전체
 * @property others [matched]에 들지 않은 좋았던 기억
 */
data class CoursePlan(
    val case: CourseCase,
    val keywords: List<String>,
    val matched: List<Memory>,
    val others: List<Memory>,
)

/**
 * 남긴 기억으로 코스의 재료를 고른다. 외부 서비스 없이 글자 맞추기로만 찾는다.
 *
 * 기획 문서의 "희망 지역의 행정구역·반경 10km"와 "업종"은 좌표와 업종 데이터가 있어야 한다.
 * 그때까지는 찾는 말이 장소 이름·주소·한마디에 들어 있는지로 대신한다.
 * 별로였던 곳은 후보에서 뺀다.
 */
object MemoryCourse {

    /**
     * @param diaries 끝난 일기 전체
     * @param relation 코스를 짤 관계. 그 관계의 기억에서만 찾는다
     */
    fun plan(utterance: String, diaries: List<Diary>, relation: Relation): CoursePlan {
        val good = diaries
            .filter { it.session.relation == relation }
            .flatMap { diary -> diary.visits.map { Memory(session = diary.session, visit = it) } }
            .filter { it.visit.visit.rating == Rating.GOOD }
            .sortedByDescending { it.visit.visit.arrivedAt }
            .distinctBy { it.visit.place.id }
        val keywords = MemoryAsk.keywordsOf(utterance).filter { it !in COURSE_WORDS }

        if (keywords.isEmpty()) {
            return CoursePlan(
                case = if (good.isEmpty()) CourseCase.C else CourseCase.A,
                keywords = keywords,
                matched = good,
                others = emptyList(),
            )
        }
        val (matched, others) = good.partition { memory -> keywords.any { memory.contains(it) } }
        return CoursePlan(
            case = when {
                matched.isNotEmpty() -> CourseCase.A
                good.isNotEmpty() -> CourseCase.B
                else -> CourseCase.C
            },
            keywords = keywords,
            matched = matched,
            others = others,
        )
    }

    private fun Memory.contains(keyword: String): Boolean =
        visit.place.name.contains(keyword) ||
            visit.place.address.orEmpty().contains(keyword) ||
            visit.visit.memoText.orEmpty().contains(keyword)

    // 코스를 부탁하는 말에 흔히 섞이는, 찾는 말이 아닌 낱말
    private val COURSE_WORDS = setOf(
        "오늘", "내일", "주말", "코스", "코스로", "짜줘", "짜", "줘", "건데", "갈건데", "가볼까", "하는데", "볼까",
        "데이트", "드라이브", "나들이", "쪽", "쪽으로", "넣어서", "넣고", "근교", "1시간", "거리", "날이라", "오는", "요리",
        "먹는", "다시", "가는", "가기", "좋은", "기억", "좋아했던", "맛집", "맛집으로",
    )
}
