package com.chagokchagok.core.designsystem.theme

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Paint as NativePaint

/** 그림자 한 겹. 시안의 `0 {offsetY} {blur} {color}`와 같다. */
@Immutable
data class ShadowLayer(
    val offsetY: Dp,
    val blur: Dp,
    val color: Color,
)

/**
 * 그림자 3단계 (Pleos L1·L2·L3).
 * 값의 기준: 기획 저장소 `04. 화면 설계 및 UI/00. 화면 설계 개요.md` 2-3.
 * 바닥에 놓인 위젯과 카드에는 쓰지 않고, 떠 있는 것에만 쓴다.
 */
@Immutable
data class ChagokElevation(
    /** L1: 작은 버튼, 칩, 지도 위 라벨 */
    val level1: List<ShadowLayer> = listOf(
        ShadowLayer(offsetY = 6.dp, blur = 24.dp, color = Color(0x14000000)),
    ),
    /** L2: 주요 버튼, 토스트, 펼침 메뉴 */
    val level2: List<ShadowLayer> = listOf(
        ShadowLayer(offsetY = 8.dp, blur = 32.dp, color = Color(0x29000000)),
        ShadowLayer(offsetY = 2.dp, blur = 2.dp, color = Color(0x14000000)),
    ),
    /** L3: 팝업, 확인 창 */
    val level3: List<ShadowLayer> = listOf(
        ShadowLayer(offsetY = 20.dp, blur = 40.dp, color = Color(0x3D000000)),
        ShadowLayer(offsetY = 2.dp, blur = 4.dp, color = Color(0x33000000)),
    ),
)

/**
 * 시안의 그림자를 겹 그대로 그린다. `background`보다 앞에 붙인다.
 *
 * 예: `Modifier.chagokShadow(ChagokTheme.elevation.level2, shape).background(color, shape)`
 */
fun Modifier.chagokShadow(layers: List<ShadowLayer>, shape: Shape): Modifier =
    drawWithCache {
        val path = Path()
            .apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
            .asAndroidPath()
        val paints = layers.map { layer ->
            val paint = NativePaint().apply {
                isAntiAlias = true
                color = layer.color.toArgb()
                val radius = blurRadiusPx(layer.blur.toPx())
                if (radius > 0f) {
                    maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
                }
            }
            paint to layer.offsetY.toPx()
        }
        onDrawBehind {
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                paints.forEach { (paint, offsetY) ->
                    nativeCanvas.save()
                    nativeCanvas.translate(0f, offsetY)
                    nativeCanvas.drawPath(path, paint)
                    nativeCanvas.restore()
                }
            }
        }
    }

/**
 * 시안의 번짐 값(px)을 [BlurMaskFilter] 반지름으로 바꾼다.
 * 시안의 번짐은 표준편차의 2배이고, BlurMaskFilter는 표준편차 = 0.57735 × 반지름 + 0.5 이다.
 */
private fun blurRadiusPx(blurPx: Float): Float = ((blurPx / 2f) - 0.5f) / 0.57735f

internal val LocalChagokElevation = staticCompositionLocalOf { ChagokElevation() }
