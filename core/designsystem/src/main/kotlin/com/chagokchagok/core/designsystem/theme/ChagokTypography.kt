package com.chagokchagok.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.R

/**
 * 앱 글꼴 Asta Sans (시안과 Pleos System UI의 글꼴).
 * 굵기를 조절할 수 있는 가변 글꼴 파일 하나(`res/font/asta_sans.ttf`, 굵기 300~800)를 굵기별로 불러 쓴다.
 * 출처: Google Fonts 공개 저장소 `ofl/astasans`. 라이선스: SIL OFL 1.1 (`core/designsystem/licenses/AstaSans-OFL.txt`).
 */
internal val ChagokFontFamily: FontFamily = FontFamily(
    astaSans(FontWeight.Normal),
    astaSans(FontWeight.Medium),
    astaSans(FontWeight.SemiBold),
    astaSans(FontWeight.Bold),
    astaSans(FontWeight.ExtraBold),
)

// 가변 글꼴의 굵기 지정(FontVariation)이 이 Compose 버전에서는 실험적 API로 표시되어 있다.
@OptIn(ExperimentalTextApi::class)
private fun astaSans(weight: FontWeight): Font =
    Font(
        resId = R.font.asta_sans,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )

/**
 * 글자 6단계.
 * 크기·굵기의 기준: 기획 저장소 `04. 화면 설계 및 UI/00. 화면 설계 개요.md` 2-2.
 * 줄 높이는 문서에 없어 여기서 정한 값이다.
 */
@Immutable
data class ChagokTypography(
    /** 큰 제목 40 · 800. 홈의 "차곡차곡" */
    val display: TextStyle = chagokTextStyle(size = 40, weight = FontWeight.ExtraBold, lineHeight = 50),
    /** 화면 제목 30 · 800. 팝업 제목, 일기 완성 제목 (시안 범위 26~30) */
    val screenTitle: TextStyle = chagokTextStyle(size = 30, weight = FontWeight.ExtraBold, lineHeight = 40),
    /** 카드 제목 24 · 800. 장소 이름 (시안 범위 22~25) */
    val cardTitle: TextStyle = chagokTextStyle(size = 24, weight = FontWeight.ExtraBold, lineHeight = 32),
    /** 본문 18 · 500. 설명 문장 */
    val body: TextStyle = chagokTextStyle(size = 18, weight = FontWeight.Medium, lineHeight = 27),
    /** 보조 16 · 600. 거리, 날짜, 안내 (시안 범위 15~16) */
    val caption: TextStyle = chagokTextStyle(size = 16, weight = FontWeight.SemiBold, lineHeight = 24),
    /** 라벨 14 · 800. 칩, 작은 제목. 가장 작은 글자 */
    val label: TextStyle = chagokTextStyle(size = 14, weight = FontWeight.ExtraBold, lineHeight = 20),
)

private fun chagokTextStyle(size: Int, weight: FontWeight, lineHeight: Int): TextStyle =
    TextStyle(
        fontFamily = ChagokFontFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = lineHeight.sp,
    )

internal val LocalChagokTypography = staticCompositionLocalOf { ChagokTypography() }
