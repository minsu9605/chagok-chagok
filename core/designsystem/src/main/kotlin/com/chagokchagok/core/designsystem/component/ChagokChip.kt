package com.chagokchagok.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.RelationPalette
import com.chagokchagok.core.designsystem.theme.chagokShadow

/** 칩의 바탕색과 글자색. */
@Immutable
data class ChagokChipColors(
    val container: Color,
    val content: Color,
)

/** 시안에 나오는 칩 색 조합. */
object ChagokChipDefaults {
    /** 관계 칩: 관계의 연한 바탕 + 진한 글자 */
    fun relation(palette: RelationPalette) = ChagokChipColors(palette.soft, palette.ink)

    /** "좋았어요", 선택된 항목 */
    @Composable
    fun mint() = ChagokChipColors(ChagokTheme.colors.mintSoft, ChagokTheme.colors.onMintSoft)

    /** "별로예요" */
    @Composable
    fun danger() = ChagokChipColors(ChagokTheme.colors.dangerSoft, ChagokTheme.colors.dangerInk)

    /** #해시태그, 주의 문구 */
    @Composable
    fun lemon() = ChagokChipColors(ChagokTheme.colors.lemon, ChagokTheme.colors.onLemon)

    /** 흰 카드 위에 놓는 기본 칩 */
    @Composable
    fun fill() = ChagokChipColors(ChagokTheme.colors.fill, ChagokTheme.colors.ink)

    /** 흰 카드 위에 놓는 옅은 칩 */
    @Composable
    fun background() = ChagokChipColors(ChagokTheme.colors.background, ChagokTheme.colors.ink)

    /** 바탕색 위에 놓는 흰 칩. `elevated = true`와 함께 쓴다. */
    @Composable
    fun white() = ChagokChipColors(ChagokTheme.colors.card, ChagokTheme.colors.ink)
}

/**
 * 알약 모양 칩. 누를 수 없는 표시용이다.
 * 가장 작은 글자는 14이다(화면 설계 개요 2-2).
 *
 * @param height 시안의 칩 높이 30 · 34 · 36 · 40 · 44
 * @param elevated 바탕색 위에 띄울 때 true (그림자 L1)
 */
@Composable
fun ChagokChip(
    text: String,
    modifier: Modifier = Modifier,
    colors: ChagokChipColors = ChagokChipDefaults.fill(),
    icon: ImageVector? = null,
    height: Dp = 30.dp,
    elevated: Boolean = false,
) {
    val shape = ChagokTheme.shapes.pill
    val shadow = if (elevated) ChagokTheme.elevation.level1 else emptyList()

    Row(
        modifier = modifier
            .chagokShadow(shadow, shape)
            .height(height)
            .background(colors.container, shape)
            .padding(horizontal = if (height >= 40.dp) 16.dp else 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (height >= 40.dp) 18.dp else 14.dp),
                tint = colors.content,
            )
        }
        Text(
            text = text,
            style = ChagokTheme.typography.label.copy(
                fontSize = when {
                    height >= 44.dp -> 16.sp
                    height >= 34.dp -> 15.sp
                    else -> 14.sp
                },
            ),
            color = colors.content,
            maxLines = 1,
        )
    }
}
