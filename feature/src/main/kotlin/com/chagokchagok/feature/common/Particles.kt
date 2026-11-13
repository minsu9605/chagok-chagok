package com.chagokchagok.feature.common

/*
 * 이름 뒤에 붙이는 조사. 이름의 마지막 글자에 받침이 있는지로 고른다.
 * 한글로 끝나지 않는 이름은 받침이 없는 것으로 본다.
 */

/** "을" 또는 "를". 예: 수성못 → "을", 앞산 카페 → "를" */
internal fun objectParticle(name: String): String = if (finalConsonant(name) == 0) "를" else "을"

/** "으로" 또는 "로". 받침이 없거나 ㄹ 받침이면 "로"다. 예: 수성못 → "으로", 앞산 카페 → "로" */
internal fun directionParticle(name: String): String =
    when (finalConsonant(name)) {
        0, RIEUL_FINAL -> "로"
        else -> "으로"
    }

/** 마지막 글자의 받침 번호. 받침이 없거나 한글이 아니면 0 */
private fun finalConsonant(name: String): Int {
    val last = name.trim().lastOrNull() ?: return 0
    if (last !in HANGUL_FIRST..HANGUL_LAST) return 0
    return (last - HANGUL_FIRST) % HANGUL_FINAL_COUNT
}

private const val HANGUL_FIRST = '가'
private const val HANGUL_LAST = '힣'
private const val HANGUL_FINAL_COUNT = 28
private const val RIEUL_FINAL = 8
