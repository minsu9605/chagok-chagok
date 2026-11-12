package com.chagokchagok.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.RelationPalette

/**
 * 탑승권 카드: 관계 색 띠(관계·날짜, 장소 이름) + 점선 아래 내용.
 * 기억 답변과 저장 결과를 보여 줄 때 쓴다(화면 13, 15, 16, 19, 27, 28).
 *
 * @param palette 관계 색
 * @param icon 관계 아이콘
 * @param overline 띠의 작은 글자. 예: "연인과 · 2026.10.12"
 * @param title 장소 이름
 * @param content 점선 아래 내용. [BoardingPassField]를 놓는다.
 */
@Composable
fun BoardingPassCard(
    palette: RelationPalette,
    icon: ImageVector,
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ChagokTheme.colors

    Column(
        modifier = modifier
            .clip(PassShape)
            .background(colors.card)
            .border(width = 1.dp, color = colors.fill, shape = PassShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.band)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.card, ChagokTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = palette.ink,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = overline,
                    style = ChagokTheme.typography.label,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title,
                    style = ChagokTheme.typography.cardTitle.copy(fontSize = 26.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DashedDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** 탑승권 칸 하나: 작은 칸 이름 + 값. 값이 글자일 때 쓴다. */
@Composable
fun BoardingPassField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    BoardingPassField(label = label, modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ChagokTheme.colors.mintInk,
                )
            }
            Text(
                text = value,
                style = ChagokTheme.typography.label.copy(fontSize = 19.sp, lineHeight = 26.sp),
                color = ChagokTheme.colors.ink,
            )
        }
    }
}

/** 탑승권 칸 하나: 작은 칸 이름 + 내용. 값이 해시태그 줄처럼 글자가 아닐 때 쓴다. */
@Composable
fun BoardingPassField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.inkSecondary,
        )
        content()
    }
}

@Composable
private fun DashedDivider() {
    val color = ChagokTheme.colors.fill

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
            ),
        )
    }
}

private val PassShape = RoundedCornerShape(26.dp)
