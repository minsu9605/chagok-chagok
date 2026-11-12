package com.chagokchagok.core.designsystem.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow

/**
 * 위젯: 바닥에 놓인 흰 카드(모서리 36). 그림자가 없다.
 * 홈의 쌓기·일기 보기·코스 짜기·기억 물어보기 위젯에 쓴다.
 */
@Composable
fun ChagokWidgetCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = ChagokTheme.shapes.widget
    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (onClick != null) {
        Modifier
            .pressScale(interactionSource)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .background(ChagokTheme.colors.card)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * 팝업: 뒤를 어둡게 덮고 가운데에 흰 창(모서리 40, 그림자 L3)을 띄운다.
 * 화면 내용 위에 겹쳐 놓는다. 부모 영역 전체를 덮으므로 `Box` 안에서 화면 내용 다음에 둔다.
 * 덮개를 눌러도 닫히지 않는다. 닫기는 [onClose]의 ✕ 버튼이나 팝업 안 버튼으로 한다.
 *
 * @param width 시안의 팝업 너비 720~780. 창이 좁으면 그에 맞춰 줄어든다.
 * @param onClose null이 아니면 오른쪽 위에 ✕ 버튼을 보인다.
 * @param closeContentDescription ✕ 버튼의 화면 낭독용 설명
 */
@Composable
fun ChagokDialog(
    modifier: Modifier = Modifier,
    width: Dp = 720.dp,
    onClose: (() -> Unit)? = null,
    closeContentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = ChagokTheme.shapes.dialog

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.scrim)
            // 덮개 뒤의 화면이 눌리지 않게 터치를 여기서 받는다.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = width)
                .fillMaxWidth()
                .chagokShadow(ChagokTheme.elevation.level3, shape)
                .background(ChagokTheme.colors.card, shape),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, top = 26.dp, end = 30.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
            if (onClose != null) {
                ChagokIconButton(
                    icon = ChagokIcons.Close,
                    contentDescription = closeContentDescription,
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp),
                )
            }
        }
    }
}

/**
 * 토스트: 흰 알림(모서리 24, 그림자 L2, 가로 최대 792).
 * 위치(화면 아래 가운데)와 띄우고 숨기는 시점은 쓰는 화면에서 정한다.
 *
 * @param onClick null이 아니면 토스트를 누를 수 있다.
 */
@Composable
fun ChagokToast(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = ChagokIcons.Logo,
    iconContainerColor: Color = ChagokTheme.colors.mint,
    iconColor: Color = ChagokTheme.colors.onMint,
    onClick: (() -> Unit)? = null,
) {
    val shape = ChagokTheme.shapes.toast
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = modifier
            .widthIn(max = 792.dp)
            .chagokShadow(ChagokTheme.elevation.level2, shape)
            .clip(shape)
            .background(ChagokTheme.colors.card)
            .then(clickModifier)
            .padding(start = 14.dp, top = 14.dp, end = 24.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(iconContainerColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = iconColor,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = ChagokTheme.typography.label.copy(fontSize = 19.sp, lineHeight = 26.sp),
                color = ChagokTheme.colors.ink,
            )
            if (message != null) {
                Text(
                    text = message,
                    style = ChagokTheme.typography.caption,
                    color = ChagokTheme.colors.inkSecondary,
                )
            }
        }
    }
}

/**
 * 남은 시간 막대. 10초 응답, 2초 저장 표시에 쓴다.
 *
 * @param progress 0(비어 있음) ~ 1(가득 참)
 */
@Composable
fun ChagokProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val shape = ChagokTheme.shapes.pill

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(ChagokTheme.colors.fill, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .background(ChagokTheme.colors.mint, shape),
        )
    }
}
