package com.chagokchagok.feature.ask

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.BoardingPassCard
import com.chagokchagok.core.designsystem.component.BoardingPassField
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipColors
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.common.objectParticle

/** 기억 물어보기에서 일어나는 동작 묶음. */
internal data class AskActions(
    val onQuestionChange: (String) -> Unit = {},
    val onQuestionSubmit: () -> Unit = {},
    val onExampleClick: (String) -> Unit = {},
    val onFreshClick: () -> Unit = {},
    val onGuideClick: () -> Unit = {},
    val onClose: () -> Unit = {},
)

/** 답이 화면에 뜨면 같은 내용을 소리로도 알려 달라고 한 번 요청한다. */
@Composable
internal fun AskAnnouncement(stage: AskStage?, onAnswerShown: (String) -> Unit) {
    val answer = stage as? AskStage.Answer ?: return
    val memory = answer.memory
    val text = when {
        memory != null -> stringResource(R.string.feature_ask_speak_found, memory.placeName, memory.spokenDateText)
        else -> noneTitle(answer)
    }
    LaunchedEffect(answer) { onAnswerShown(text) }
}

/** 듣는 중: 마이크, 소리 막대, 받아 적은 질문. 같은 자리를 눌러 글자로 적을 수도 있다. */
@Composable
internal fun AskListeningBody(
    listening: AskStage.Listening,
    micSize: Dp,
    onQuestionChange: (String) -> Unit,
    onQuestionSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circle = ChagokTheme.shapes.pill
    val textStyle = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 27.sp, textAlign = TextAlign.Center)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                if (listening.listening) R.string.feature_ask_listening else R.string.feature_ask_typing,
            ),
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.mintInk,
        )
        Box(
            modifier = Modifier
                .size(micSize * 1.36f)
                .background(ChagokTheme.colors.mintSoft, circle),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(micSize)
                    .chagokShadow(ChagokTheme.elevation.level2, circle)
                    .background(ChagokTheme.colors.mint, circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(micSize * 0.42f),
                    tint = ChagokTheme.colors.onMint,
                )
            }
        }
        SoundBars(moving = listening.listening)
        Box(contentAlignment = Alignment.Center) {
            if (listening.text.isEmpty()) {
                Text(
                    text = stringResource(
                        if (listening.listening) {
                            R.string.feature_ask_placeholder
                        } else {
                            R.string.feature_ask_placeholder_typing
                        },
                    ),
                    style = textStyle,
                    color = ChagokTheme.colors.inkSecondary,
                )
            }
            BasicTextField(
                value = listening.text,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle.copy(color = ChagokTheme.colors.ink),
                cursorBrush = SolidColor(ChagokTheme.colors.mint),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onQuestionSubmit() }),
                maxLines = 2,
            )
        }
    }
}

/** 소리 막대 8개. 듣는 동안 오르내린다. */
@Composable
private fun SoundBars(moving: Boolean) {
    val transition = rememberInfiniteTransition(label = "soundBars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 700), repeatMode = RepeatMode.Reverse),
        label = "soundBarsPhase",
    )

    Row(
        modifier = Modifier.height(BAR_MAX_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BAR_LEVELS.forEachIndexed { index, level ->
            // 이웃한 막대가 엇갈려 움직이게 한다.
            val swing = if (index % 2 == 0) phase else 1f - phase
            val scale = if (moving) 0.45f + 0.55f * swing else 1f
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(BAR_MAX_HEIGHT * level * scale)
                    .background(ChagokTheme.colors.mint, ChagokTheme.shapes.pill),
            )
        }
    }
}

/** "지금은 연인 모드라 연인과 다녀온 곳 중 5km 안에서 찾아요" */
@Composable
internal fun scopeText(scopeRelation: RelationUi?): String =
    if (scopeRelation == null) {
        stringResource(R.string.feature_ask_scope_all)
    } else {
        stringResource(
            R.string.feature_ask_scope_relation,
            stringResource(scopeRelation.modeLabelRes),
            stringResource(scopeRelation.withLabelRes),
        )
    }

/** 누르면 그 질문을 바로 묻는 예시 칩. */
@Composable
internal fun AskExampleChip(
    question: String,
    containerColor: Color,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(ChagokTheme.shapes.pill)
            .background(containerColor)
            .clickable(role = Role.Button) { onClick(question) }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.feature_ask_quote, question),
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

/**
 * 기억을 보여 주는 탑승권 카드.
 *
 * @param detailed 남긴 평가와 한마디까지 보여 주면 true. 다른 업종만 있을 때(화면 16)는 false
 */
@Composable
internal fun AskMemoryPass(
    memory: AskMemoryUi,
    modifier: Modifier = Modifier,
    detailed: Boolean = true,
) {
    val relation = memory.relation

    BoardingPassCard(
        palette = relation.palette,
        icon = relation.icon,
        overline = if (relation == RelationUi.SOLO) {
            stringResource(R.string.feature_ask_pass_overline_solo, memory.dateText)
        } else {
            stringResource(
                R.string.feature_ask_pass_overline,
                stringResource(relation.withLabelRes),
                memory.dateText,
            )
        },
        title = memory.placeName,
        modifier = modifier,
    ) {
        // 거리는 좌표가 있어야 잴 수 있어서 넣지 않았다. 해시태그도 아직 만들지 않는다.
        BoardingPassField(
            label = stringResource(R.string.feature_ask_pass_rating),
            value = stringResource(if (memory.good) R.string.feature_memory_good else R.string.feature_memory_bad),
            icon = if (memory.good) ChagokIcons.ThumbUp else ChagokIcons.ThumbDown,
        )
        if (detailed && memory.memo != null) {
            Text(
                text = stringResource(R.string.feature_ask_quote, memory.memo),
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = ChagokTheme.colors.ink,
            )
        }
    }
}

/** "근처에 1곳 더 있어요" */
@Composable
internal fun AskMoreChip(moreCount: Int, colors: ChagokChipColors) {
    ChagokChip(
        text = stringResource(R.string.feature_ask_more, moreCount),
        colors = colors,
        icon = ChagokIcons.Pin,
        height = 36.dp,
    )
}

/** 기록이 없을 때의 안내: (별로였던 곳을 뺐다는 칩) + 제목 + 설명. */
@Composable
internal fun AskNoneNotice(answer: AskStage.Answer, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (answer.badSkipped) {
            ChagokChip(
                text = stringResource(R.string.feature_ask_bad_skipped),
                colors = ChagokChipDefaults.fill(),
                icon = ChagokIcons.ThumbDown,
                height = 34.dp,
            )
        }
        Text(
            text = noneTitle(answer),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 28.sp),
            color = ChagokTheme.colors.ink,
        )
        Text(
            text = if (answer.keywordLabel == null) {
                stringResource(R.string.feature_ask_none_recall_description)
            } else {
                stringResource(
                    R.string.feature_ask_none_find_description,
                    answer.keywordLabel + objectParticle(answer.keywordLabel),
                )
            },
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, lineHeight = 23.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

@Composable
private fun noneTitle(answer: AskStage.Answer): String =
    stringResource(
        if (answer.keywordLabel == null) {
            R.string.feature_ask_none_recall_title
        } else {
            R.string.feature_ask_none_find_title
        },
    )

/** 새 장소 찾기 자리의 안내 (화면 18). 지도 서비스를 붙이기 전이라 찾지 못한다고 알린다. */
@Composable
internal fun AskFreshNotice(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = ChagokIcons.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = ChagokTheme.colors.inkSecondary,
        )
        Text(
            text = stringResource(R.string.feature_ask_fresh_pending_title),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 28.sp),
            color = ChagokTheme.colors.ink,
        )
        Text(
            text = stringResource(R.string.feature_ask_fresh_pending_description),
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, lineHeight = 23.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

/** 작은 안내 한 줄: 아이콘 + 글. 예: 🎤 "응"이라고 말해도 돼요 */
@Composable
internal fun AskHint(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = ChagokTheme.colors.mintInk,
        )
        Text(
            text = text,
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

private val BAR_MAX_HEIGHT = 30.dp
private val BAR_LEVELS = listOf(0.35f, 0.6f, 0.9f, 1f, 0.7f, 0.95f, 0.55f, 0.3f)
