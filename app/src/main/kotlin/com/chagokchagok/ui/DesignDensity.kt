package com.chagokchagok.ui

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.max

/**
 * 화면 시안의 크기 기준에 맞춰 앱 전체의 밀도를 정한다.
 *
 * 시안은 디스플레이의 긴 변(2560px)을 1280dp로 보고 그렸다(화면 설계 개요 1장: 시안의 1px = 1dp).
 * Pleos 에뮬레이터는 2560px 화면의 밀도가 160dpi(1dp = 1px)라서, 시스템 밀도를 그대로 쓰면
 * 글자와 부품이 시안의 절반 크기로 보인다. 그래서 "디스플레이 긴 변 = 1280dp"가 되도록 밀도를 다시 정한다.
 *
 * 디스플레이 크기를 읽지 못하면 시스템 밀도를 그대로 쓴다.
 */
@Composable
fun ProvideDesignDensity(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val systemDensity = LocalDensity.current
    val designDensity = remember(context, systemDensity) {
        val longSidePx = context.displayLongSidePx()
        if (longSidePx == null) {
            systemDensity
        } else {
            Density(
                density = longSidePx / DESIGN_LONG_SIDE_DP,
                fontScale = systemDensity.fontScale,
            )
        }
    }

    CompositionLocalProvider(LocalDensity provides designDensity, content = content)
}

/** 창 크기가 아니라 디스플레이 자체의 긴 변 길이(px). 분할 화면에서도 값이 같다. */
private fun Context.displayLongSidePx(): Int? {
    val displayManager = getSystemService(DisplayManager::class.java) ?: return null
    val mode = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.mode ?: return null
    return max(mode.physicalWidth, mode.physicalHeight)
}

private const val DESIGN_LONG_SIDE_DP = 1280f
