package com.chagokchagok.feature.record.memory

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chagokchagok.core.designsystem.component.BoardingPassCard
import com.chagokchagok.core.designsystem.component.BoardingPassField
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokDialog
import com.chagokchagok.core.designsystem.component.ChagokProgressBar
import com.chagokchagok.core.designsystem.component.pressScale
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import kotlin.math.ceil

/**
 * 기억 남기기 팝업을 띄우는 자리 (화면 23~28). 어느 화면에 있든 그 위에 뜨도록 화면들 위에 겹쳐 놓는다.
 * 띄울 팝업이 없으면 아무것도 그리지 않는다.
 */
@Composable
fun MemoryPromptHost(
    modifier: Modifier = Modifier,
    viewModel: MemoryPromptViewModel = hiltViewModel(),
) {
    val prompt by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = prompt) {
        null -> Unit
        is MemoryPromptUi.Rate -> RateDialog(
            rate = current,
            onRateClick = viewModel::onRateClick,
            onCloseClick = viewModel::onRateClose,
            modifier = modifier,
        )
        is MemoryPromptUi.Memo -> MemoDialog(
            memo = current,
            onTextChange = viewModel::onMemoTextChange,
            onSaveClick = viewModel::onMemoSaveClick,
            onLaterClick = viewModel::onMemoLaterClick,
            modifier = modifier,
        )
        is MemoryPromptUi.Saved -> SavedDialog(saved = current, modifier = modifier)
    }
}

/** ① 좋았어요·별로예요 (화면 23). 10초 안에 고르지 않으면 기록하지 않는다. */
@Composable
private fun RateDialog(
    rate: MemoryPromptUi.Rate,
    onRateClick: (good: Boolean) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = rememberCountdown(key = rate.visitId, durationMillis = MemoryPromptViewModel.RATE_TIMEOUT_MILLIS)

    ChagokDialog(
        modifier = modifier,
        width = 700.dp,
        onClose = onCloseClick,
        closeContentDescription = stringResource(R.string.feature_memory_close),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChagokChip(
                text = stringResource(rate.relation.withLabelRes),
                colors = ChagokChipDefaults.relation(rate.relation.palette),
                icon = rate.relation.icon,
                height = 34.dp,
            )
            Text(
                text = stringResource(R.string.feature_memory_rate_meta, rate.arrivedAtText, rate.stayMinutes),
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Text(
            text = stringResource(R.string.feature_memory_rate_title, rate.placeName),
            style = ChagokTheme.typography.screenTitle.copy(fontSize = 28.sp, lineHeight = 38.sp),
            color = ChagokTheme.colors.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RateButton(
                text = stringResource(R.string.feature_memory_good),
                icon = ChagokIcons.ThumbUp,
                containerColor = ChagokTheme.colors.mint,
                contentColor = ChagokTheme.colors.onMint,
                elevated = true,
                onClick = { onRateClick(true) },
                modifier = Modifier.weight(1f),
            )
            RateButton(
                text = stringResource(R.string.feature_memory_bad),
                icon = ChagokIcons.ThumbDown,
                containerColor = ChagokTheme.colors.fill,
                contentColor = ChagokTheme.colors.ink,
                elevated = false,
                onClick = { onRateClick(false) },
                modifier = Modifier.weight(1f),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChagokProgressBar(progress = remaining)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.feature_memory_rate_timeout_hint),
                    style = ChagokTheme.typography.label,
                    color = ChagokTheme.colors.inkSecondary,
                )
                Text(
                    text = stringResource(
                        R.string.feature_memory_seconds,
                        ceil(remaining * MemoryPromptViewModel.RATE_TIMEOUT_MILLIS / MILLIS_PER_SECOND).toInt(),
                    ),
                    style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = ChagokTheme.colors.ink,
                )
            }
        }
    }
}

@Composable
private fun RateButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    elevated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = ChagokTheme.shapes.card
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Column(
        modifier = modifier
            .pressScale(interactionSource)
            .chagokShadow(if (elevated) ChagokTheme.elevation.level2 else emptyList(), shape)
            .height(140.dp)
            .clip(shape)
            .background(containerColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = contentColor)
        Text(
            text = text,
            style = ChagokTheme.typography.cardTitle,
            color = contentColor,
        )
    }
}

/**
 * ② 한마디 (화면 24, 25).
 * 시안은 음성으로 받는다. 음성 연결 전이라 같은 자리에 글자 입력칸과 [저장]을 임시로 둔다.
 */
@Composable
internal fun MemoDialog(
    memo: MemoryPromptUi.Memo,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circle = ChagokTheme.shapes.pill

    ChagokDialog(modifier = modifier, width = 700.dp) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChagokChip(
                text = stringResource(if (memo.good) R.string.feature_memory_good else R.string.feature_memory_bad),
                colors = if (memo.good) ChagokChipDefaults.mint() else ChagokChipDefaults.fill(),
                icon = if (memo.good) ChagokIcons.ThumbUp else ChagokIcons.ThumbDown,
                height = 34.dp,
            )
            Text(
                text = memo.placeName,
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(
                    if (memo.good) R.string.feature_memory_memo_title_good else R.string.feature_memory_memo_title_bad,
                ),
                style = ChagokTheme.typography.screenTitle.copy(fontSize = 28.sp, lineHeight = 38.sp),
                color = ChagokTheme.colors.ink,
            )
            Text(
                text = stringResource(
                    if (memo.good) R.string.feature_memory_memo_example_good else R.string.feature_memory_memo_example_bad,
                ),
                style = ChagokTheme.typography.caption,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChagokTheme.colors.background, ChagokTheme.shapes.card)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .chagokShadow(ChagokTheme.elevation.level2, circle)
                    .background(ChagokTheme.colors.mint, circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.feature_memory_memo_input_label),
                    style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = ChagokTheme.colors.mintInk,
                )
                Box {
                    if (memo.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.feature_memory_memo_input_placeholder),
                            style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp),
                            color = ChagokTheme.colors.inkSecondary,
                        )
                    }
                    BasicTextField(
                        value = memo.text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = ChagokTheme.typography.cardTitle.copy(
                            fontSize = 21.sp,
                            color = ChagokTheme.colors.ink,
                        ),
                        cursorBrush = SolidColor(ChagokTheme.colors.mint),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSaveClick() }),
                        singleLine = true,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ChagokIcons.Clock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = stringResource(R.string.feature_memory_memo_auto_save_hint),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChagokButton(
                text = stringResource(R.string.feature_memory_memo_later),
                onClick = onLaterClick,
                modifier = Modifier.weight(1f),
                style = ChagokButtonStyle.Secondary,
                height = 60.dp,
            )
            ChagokButton(
                text = stringResource(R.string.feature_memory_memo_save),
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                height = 60.dp,
                enabled = memo.text.isNotBlank(),
            )
        }
    }
}

/** ③ 저장 완료 (화면 28). 2초 뒤 저절로 닫힌다. */
@Composable
private fun SavedDialog(saved: MemoryPromptUi.Saved, modifier: Modifier = Modifier) {
    val remaining = rememberCountdown(key = saved, durationMillis = MemoryPromptViewModel.SAVED_VISIBLE_MILLIS)
    val circle = ChagokTheme.shapes.pill

    ChagokDialog(modifier = modifier, width = 700.dp) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ChagokTheme.colors.mint, circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
            Text(
                text = stringResource(R.string.feature_memory_saved_title, saved.placeName),
                style = ChagokTheme.typography.cardTitle,
                color = ChagokTheme.colors.ink,
            )
        }
        BoardingPassCard(
            palette = saved.relation.palette,
            icon = saved.relation.icon,
            overline = stringResource(
                R.string.feature_memory_saved_overline,
                stringResource(saved.relation.withLabelRes),
                saved.arrivedAtText,
            ),
            title = saved.placeName,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                BoardingPassField(
                    label = stringResource(R.string.feature_memory_saved_rating_label),
                    value = stringResource(
                        if (saved.good) R.string.feature_memory_good else R.string.feature_memory_bad,
                    ),
                    icon = if (saved.good) ChagokIcons.ThumbUp else ChagokIcons.ThumbDown,
                )
                BoardingPassField(label = stringResource(R.string.feature_memory_saved_tag_label)) {
                    ChagokChip(
                        text = stringResource(R.string.feature_memory_saved_tag_pending),
                        colors = ChagokChipDefaults.fill(),
                    )
                }
            }
            if (saved.memo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.feature_memory_saved_memo_quote, saved.memo),
                    style = ChagokTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = ChagokTheme.colors.ink,
                )
            }
        }
        Text(
            text = stringResource(R.string.feature_memory_saved_note),
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.inkSecondary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChagokProgressBar(progress = remaining, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.feature_memory_saved_closing),
                style = ChagokTheme.typography.label,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
    }
}

/** [durationMillis] 동안 1에서 0으로 줄어드는 값. [key]가 바뀌면 다시 센다. */
@Composable
private fun rememberCountdown(key: Any, durationMillis: Long): Float {
    val remaining = remember(key) { Animatable(1f) }
    LaunchedEffect(key) {
        remaining.animateTo(0f, tween(durationMillis = durationMillis.toInt(), easing = LinearEasing))
    }
    return remaining.value
}

private const val MILLIS_PER_SECOND = 1_000f

@Preview(name = "23 호불호", widthDp = 841, heightDp = 605)
@Composable
private fun RateDialogPreview() = ChagokTheme {
    RateDialog(
        rate = MemoryPromptUi.Rate(
            visitId = "1",
            relation = RelationUi.COUPLE,
            placeName = "앞산 카페",
            arrivedAtText = "13:05",
            stayMinutes = 52,
            arrivedDateTimeText = "2026.11.09 13:05",
        ),
        onRateClick = {},
        onCloseClick = {},
    )
}

@Preview(name = "24 한마디 · 좋았어요", widthDp = 841, heightDp = 605)
@Composable
private fun MemoDialogPreview() = ChagokTheme {
    MemoDialog(
        memo = MemoryPromptUi.Memo(
            visitId = "1",
            placeName = "앞산 카페",
            good = true,
            text = "여기 치즈케이크가 진짜 꾸덕하고 맛있었어",
        ),
        onTextChange = {},
        onSaveClick = {},
        onLaterClick = {},
    )
}

@Preview(name = "28 저장 완료", widthDp = 841, heightDp = 605)
@Composable
private fun SavedDialogPreview() = ChagokTheme {
    SavedDialog(
        saved = MemoryPromptUi.Saved(
            relation = RelationUi.COUPLE,
            placeName = "앞산 카페",
            arrivedAtText = "2026.11.09 13:05",
            good = true,
            memo = "여기 치즈케이크가 진짜 꾸덕하고 맛있었어",
        ),
    )
}
