package com.chagokchagok.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 모서리.
 * 값의 기준: 기획 저장소 `04. 화면 설계 및 UI/00. 화면 설계 개요.md` 2-3.
 * 안쪽 모서리 = 바깥 모서리 − 여백.
 */
@Immutable
data class ChagokShapes(
    /** 위젯 36 */
    val widget: Shape = RoundedCornerShape(36.dp),
    /** 카드 28 */
    val card: Shape = RoundedCornerShape(28.dp),
    /** 안쪽 카드 22 */
    val innerCard: Shape = RoundedCornerShape(22.dp),
    /** 팝업 40 */
    val dialog: Shape = RoundedCornerShape(40.dp),
    /** 확인 창 32 */
    val alert: Shape = RoundedCornerShape(32.dp),
    /** 토스트 24 */
    val toast: Shape = RoundedCornerShape(24.dp),
    /** 버튼·칩: 알약 (높이의 절반) */
    val pill: Shape = CircleShape,
)

internal val LocalChagokShapes = staticCompositionLocalOf { ChagokShapes() }
