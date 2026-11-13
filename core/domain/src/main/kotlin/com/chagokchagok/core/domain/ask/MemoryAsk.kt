package com.chagokchagok.core.domain.ask

import com.chagokchagok.core.domain.model.Diary
import com.chagokchagok.core.domain.model.DriveSession
import com.chagokchagok.core.domain.model.Rating
import com.chagokchagok.core.domain.model.Relation
import com.chagokchagok.core.domain.model.VisitWithPlace

/** 질문의 종류. */
enum class AskKind {
    /** 회상: "우리 여기 전에 왔었나?" */
    RECALL,

    /** 장소 찾기: "치즈케이크 카페 어디였지?" */
    FIND,
}

/** 답의 종류. */
enum class AskAnswerType {
    /** 찾는 기록이 있다 */
    FOUND,

    /** 찾는 기록은 없지만 다른 좋았던 기록이 있다 (장소 찾기에서만) */
    OTHER,

    /** 기록이 없다 */
    NONE,
}

/** 기억 하나 = 끝난 일기의 방문 하나. */
data class Memory(
    val session: DriveSession,
    val visit: VisitWithPlace,
)

/**
 * 질문의 답.
 *
 * @property keywords 질문에서 뽑은 찾는 말. 회상 질문이면 비어 있다
 * @property hit 보여 줄 기억 하나. [AskAnswerType.NONE]이면 null
 * @property moreCount [hit] 말고 더 있는 장소 수
 * @property badSkipped 별로였던 곳을 빼고 찾았으면 true (장소 찾기에서 찾는 곳이 없을 때만)
 */
data class AskAnswer(
    val question: String,
    val kind: AskKind,
    val type: AskAnswerType,
    val keywords: List<String>,
    val hit: Memory?,
    val moreCount: Int,
    val badSkipped: Boolean,
)

/**
 * 남긴 기억에서 질문의 답을 찾는다. 외부 서비스 없이 글자 맞추기로만 찾는다.
 *
 * 기획 문서의 "현재 위치 5km 안"과 "업종"은 좌표와 업종 데이터가 있어야 한다. 그때까지는 이렇게 대신한다.
 * - 거리로 거르지 않고 전체 기록에서 찾는다.
 * - 회상 질문의 "여기"는 가장 최근에 도착한 곳으로 본다.
 * - 장소 찾기는 찾는 말이 장소 이름이나 한마디에 들어 있는지로 본다.
 */
object MemoryAsk {

    /**
     * @param diaries 끝난 일기 전체
     * @param relation 지금 기록 중인 관계. 그 관계의 기억에서만 찾는다. 기록 중이 아니면 null (전체에서 찾음)
     * @param herePlaceId 가장 최근에 도착한 곳. 없으면 null
     */
    fun answer(
        question: String,
        diaries: List<Diary>,
        relation: Relation?,
        herePlaceId: String?,
    ): AskAnswer {
        val memories = diaries
            .filter { relation == null || it.session.relation == relation }
            .flatMap { diary -> diary.visits.map { Memory(session = diary.session, visit = it) } }
            .sortedByDescending { it.visit.visit.arrivedAt }
        val keywords = keywordsOf(question)

        if (keywords.isEmpty()) {
            // 회상 질문은 별로였던 기록도 답에 넣는다.
            val here = memories.filter { it.visit.place.id == herePlaceId }
            return AskAnswer(
                question = question,
                kind = AskKind.RECALL,
                type = if (here.isEmpty()) AskAnswerType.NONE else AskAnswerType.FOUND,
                keywords = keywords,
                hit = here.firstOrNull(),
                moreCount = 0,
                badSkipped = false,
            )
        }

        // 장소 찾기는 좋았던 기록만 센다.
        val good = memories.filter { it.visit.visit.rating == Rating.GOOD }
        val matched = good
            .map { memory -> memory to keywords.count { memory.contains(it) } }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }
            .map { (memory, _) -> memory }
        val (type, list) = when {
            matched.isNotEmpty() -> AskAnswerType.FOUND to matched
            good.isNotEmpty() -> AskAnswerType.OTHER to good
            else -> AskAnswerType.NONE to emptyList()
        }
        val places = list.distinctBy { it.visit.place.id }
        return AskAnswer(
            question = question,
            kind = AskKind.FIND,
            type = type,
            keywords = keywords,
            hit = places.firstOrNull(),
            moreCount = (places.size - 1).coerceAtLeast(0),
            badSkipped = type != AskAnswerType.FOUND && memories.any { it.visit.visit.rating == Rating.BAD },
        )
    }

    private fun Memory.contains(keyword: String): Boolean =
        visit.place.name.contains(keyword) || visit.visit.memoText.orEmpty().contains(keyword)

    /** 질문에서 찾는 말만 남긴다. 조사와 물음말, 흔한 꾸밈말은 뺀다. */
    fun keywordsOf(question: String): List<String> =
        question
            .split(WORD_BREAK)
            .map { it.trim() }
            .filter { it.isNotEmpty() && it !in STOP_WORDS }
            .map { word -> PARTICLES.firstOrNull { word.length > it.length + 1 && word.endsWith(it) }?.let(word::removeSuffix) ?: word }
            .filter { it.length >= MIN_KEYWORD_LENGTH && it !in STOP_WORDS }
            .distinct()

    private const val MIN_KEYWORD_LENGTH = 2
    private val WORD_BREAK = Regex("[\\s?!.,~\"“”‘’']+")

    // 긴 것부터 맞춰 본다.
    private val PARTICLES = listOf("에서", "으로", "이랑", "하고", "은", "는", "이", "가", "을", "를", "에", "의", "도", "로", "랑")

    private val STOP_WORDS = setOf(
        "우리", "나", "내가", "여기", "거기", "저기", "이", "그", "저", "이곳", "그곳", "근처", "근처에", "이근처", "동네", "이동네",
        "전에", "예전에", "지난번", "지난번에", "저번", "저번에", "옛날에", "언제", "혹시", "좀", "한번",
        "갔던", "갔었던", "왔던", "왔었던", "가본", "와본", "다녀온", "먹었던", "봤던", "좋았던", "맛있던", "맛있는", "맛있었던", "괜찮았던",
        "왔었나", "왔었어", "왔었지", "왔나", "왔어", "와봤나", "와봤어", "갔었나", "갔었어", "갔나",
        "어디", "어디야", "어디였지", "어디였어", "어디였더라", "어디지", "뭐였지", "뭐였더라", "이름이", "이름", "기억나", "기억나니",
        "만한", "갈만한", "찾아줘", "찾아", "알려줘", "말해줘", "보여줘", "있어", "있나", "있었나", "있었지", "곳", "데", "거", "집",
    )
}
