package com.chagokchagok.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.ShadowLayer
import com.chagokchagok.core.designsystem.theme.chagokShadow

/** 버튼 종류. 시안의 주요(민트) · 보조(채움) · 흰색 · 연한 민트 버튼. */
enum class ChagokButtonStyle {
    /** 민트 바탕. 화면의 주요 동작 */
    Primary,

    /** 채움 바탕. 닫기 같은 보조 동작 */
    Secondary,

    /** 흰 바탕 + 그림자 L1. 바탕색 위에 놓는 버튼 */
    White,

    /** 연한 민트 바탕. 카드 안의 덜 강한 동작 */
    Soft,
}

/**
 * 알약 모양 버튼.
 * 운전 중 누르는 주요 버튼은 높이 64 이상, 모든 터치 영역은 44 이상이어야 한다(화면 설계 개요 2-4).
 *
 * @param elevated 민트 버튼을 띄워 보이게 할 때 true (그림자 L2)
 */
@Composable
fun ChagokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChagokButtonStyle = ChagokButtonStyle.Primary,
    height: Dp = 64.dp,
    icon: ImageVector? = null,
    elevated: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = style.colors()
    val shape = ChagokTheme.shapes.pill

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .pressScale(interactionSource)
            .chagokShadow(style.shadow(elevated), shape)
            .height(height)
            .clip(shape)
            .background(colors.container)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = if (height >= 60.dp) 26.dp else 22.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (height >= 60.dp) 24.dp else 20.dp),
                tint = colors.content,
            )
        }
        Text(
            text = text,
            style = ChagokTheme.typography.label.copy(
                fontSize = if (height >= 60.dp) 20.sp else 18.sp,
                lineHeight = if (height >= 60.dp) 28.sp else 26.sp,
            ),
            color = colors.content,
            maxLines = 1,
        )
    }
}

/**
 * 동그란 아이콘 버튼. 닫기(✕), 마이크 등에 쓴다.
 *
 * @param contentDescription 화면 낭독용 설명. 꾸밈용이면 null
 */
@Composable
fun ChagokIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChagokButtonStyle = ChagokButtonStyle.Secondary,
    size: Dp = 52.dp,
    iconSize: Dp = 22.dp,
    elevated: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = style.colors()
    val shape = ChagokTheme.shapes.pill

    Box(
        modifier = modifier
            .pressScale(interactionSource)
            .chagokShadow(style.shadow(elevated), shape)
            .size(size)
            .clip(shape)
            .background(colors.container)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = colors.content,
        )
    }
}

/** 누르는 동안 95%로 살짝 줄어드는 반응 (화면 설계 개요 2-4). */
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESSED_SCALE else 1f,
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private data class ButtonColors(val container: Color, val content: Color)

@Composable
private fun ChagokButtonStyle.colors(): ButtonColors {
    val colors = ChagokTheme.colors
    return when (this) {
        ChagokButtonStyle.Primary -> ButtonColors(colors.mint, colors.onMint)
        ChagokButtonStyle.Secondary -> ButtonColors(colors.fill, colors.ink)
        ChagokButtonStyle.White -> ButtonColors(colors.card, colors.ink)
        ChagokButtonStyle.Soft -> ButtonColors(colors.mintSoft, colors.onMintSoft)
    }
}

@Composable
private fun ChagokButtonStyle.shadow(elevated: Boolean): List<ShadowLayer> = when {
    elevated -> ChagokTheme.elevation.level2
    this == ChagokButtonStyle.White -> ChagokTheme.elevation.level1
    else -> emptyList()
}

private const val PRESSED_SCALE = 0.95f
private const val DISABLED_ALPHA = 0.4f
