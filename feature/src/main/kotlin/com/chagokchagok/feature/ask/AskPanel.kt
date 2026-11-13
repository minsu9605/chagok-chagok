package com.chagokchagok.feature.ask

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokIconButton
import com.chagokchagok.core.designsystem.component.ChagokProgressBar
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.domain.ask.AskAnswerType
import com.chagokchagok.core.domain.ask.AskKind
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette

/**
 * 주행 화면의 미니 패널 자리에 뜨는 기억 물어보기 (화면 12 듣는 중, 13~17 답변, 18 새 장소).
 */
@Composable
internal fun AskPanel(
    stage: AskStage,
    scopeRelation: RelationUi?,
    actions: AskActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (stage) {
            is AskStage.Listening -> ListeningPanel(stage, scopeRelation, actions)
            is AskStage.Answer -> AnswerPanel(stage, actions)
            is AskStage.Fresh -> FreshPanel(actions)
        }
    }
}

@Composable
private fun ColumnScope.ListeningPanel(
    listening: AskStage.Listening,
    scopeRelation: RelationUi?,
    actions: AskActions,
) {
    PanelHeader(
        title = stringResource(R.string.feature_ask_title),
        icon = ChagokIcons.Mic,
        badge = false,
        onClose = actions.onClose,
    )
    AskListeningBody(
        listening = listening,
        micSize = 96.dp,
        onQuestionChange = actions.onQuestionChange,
        onQuestionSubmit = actions.onQuestionSubmit,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, ChagokTheme.shapes.widget)
            .padding(16.dp),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, PanelCardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(scopeRelation?.palette?.soft ?: ChagokTheme.colors.mintSoft, ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = scopeRelation?.icon ?: ChagokIcons.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = scopeRelation?.palette?.ink ?: ChagokTheme.colors.onMintSoft,
            )
        }
        Text(
            text = scopeText(scopeRelation),
            style = ChagokTheme.typography.label.copy(lineHeight = 20.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AskExampleChip(
            question = stringResource(R.string.feature_ask_example_visited),
            containerColor = ChagokTheme.colors.card,
            onClick = actions.onExampleClick,
        )
        AskExampleChip(
            question = stringResource(R.string.feature_ask_example_cafe),
            containerColor = ChagokTheme.colors.card,
            onClick = actions.onExampleClick,
        )
    }
}

@Composable
private fun ColumnScope.AnswerPanel(answer: AskStage.Answer, actions: AskActions) {
    val memory = answer.memory

    PanelHeader(
        title = stringResource(R.string.feature_ask_answering),
        icon = ChagokIcons.Speaker,
        badge = true,
        onClose = actions.onClose,
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_ask_quote, answer.question),
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(max = 300.dp)
                .background(ChagokTheme.colors.mintSoft, BubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = ChagokTheme.typography.label.copy(lineHeight = 21.sp),
            color = ChagokTheme.colors.ink,
        )
        when {
            memory == null -> AskNoneNotice(
                answer = answer,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChagokTheme.colors.card, PanelCardShape)
                    .padding(18.dp),
            )
            else -> {
                if (answer.type == AskAnswerType.OTHER) {
                    Text(
                        text = stringResource(R.string.feature_ask_other_lead, answer.keywordLabel.orEmpty()),
                        style = ChagokTheme.typography.caption.copy(lineHeight = 23.sp),
                        color = ChagokTheme.colors.ink,
                    )
                }
                AskMemoryPass(memory = memory, detailed = answer.type != AskAnswerType.OTHER)
                if (answer.moreCount > 0) {
                    AskMoreChip(moreCount = answer.moreCount, colors = ChagokChipDefaults.white())
                }
            }
        }
        if (memory == null && answer.kind == AskKind.FIND) {
            AskHint(text = stringResource(R.string.feature_ask_consent), icon = ChagokIcons.Lock)
        }
    }
    when {
        memory == null -> PanelChoice(
            yesText = stringResource(
                if (answer.kind == AskKind.RECALL) R.string.feature_ask_yes_recommend else R.string.feature_ask_yes_find,
            ),
            noText = stringResource(R.string.feature_ask_no),
            hint = stringResource(R.string.feature_ask_yes_hint),
            onYesClick = actions.onFreshClick,
            onNoClick = actions.onClose,
        )
        answer.type == AskAnswerType.OTHER -> PanelChoice(
            question = stringResource(R.string.feature_ask_other_question),
            yesText = stringResource(R.string.feature_ask_guide_here),
            yesIcon = ChagokIcons.Navigation,
            noText = stringResource(R.string.feature_ask_find_new),
            hint = stringResource(R.string.feature_ask_find_new_hint),
            onYesClick = actions.onGuideClick,
            onNoClick = actions.onFreshClick,
        )
        answer.kind == AskKind.RECALL -> {
            // 5초 동안 줄어드는 막대. 다 줄면 뷰모델이 패널을 닫는다.
            val progress = remember(answer) { Animatable(1f) }
            LaunchedEffect(answer) {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = AskViewModel.RECALL_VISIBLE_MILLIS.toInt(),
                        easing = LinearEasing,
                    ),
                )
            }
            ChagokProgressBar(progress = progress.value)
            Text(
                text = stringResource(R.string.feature_ask_auto_close),
                style = ChagokTheme.typography.label,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        else -> PanelChoice(
            question = stringResource(R.string.feature_ask_guide_question),
            yesText = stringResource(R.string.feature_ask_change_destination),
            yesIcon = ChagokIcons.Target,
            noText = stringResource(R.string.feature_ask_close),
            hint = stringResource(R.string.feature_ask_guide_hint),
            onYesClick = actions.onGuideClick,
            onNoClick = actions.onClose,
        )
    }
}

@Composable
private fun ColumnScope.FreshPanel(actions: AskActions) {
    PanelHeader(
        title = stringResource(R.string.feature_ask_fresh_title),
        icon = ChagokIcons.Search,
        badge = true,
        onClose = actions.onClose,
    )
    AskFreshNotice(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, PanelCardShape)
            .padding(18.dp),
    )
    Box(modifier = Modifier.weight(1f))
    AskHint(
        text = stringResource(R.string.feature_ask_fresh_hint),
        icon = ChagokIcons.Mic,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

/**
 * 패널 머리: 아이콘 + 제목 + [✕].
 *
 * @param badge 아이콘을 민트 동그라미 안에 넣으면 true
 */
@Composable
private fun PanelHeader(
    title: String,
    icon: ImageVector,
    badge: Boolean,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (badge) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ChagokTheme.colors.mint, ChagokTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ChagokTheme.colors.mintInk,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 26.sp),
            color = ChagokTheme.colors.ink,
        )
        ChagokIconButton(
            icon = ChagokIcons.Close,
            contentDescription = stringResource(R.string.feature_ask_close),
            onClick = onClose,
            style = ChagokButtonStyle.White,
            size = 44.dp,
        )
    }
}

/** 패널 아래의 물음 + 버튼 둘 + 말로 답하는 법 안내. */
@Composable
private fun ColumnScope.PanelChoice(
    yesText: String,
    noText: String,
    hint: String,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    question: String? = null,
    yesIcon: ImageVector? = null,
) {
    if (question != null) {
        Text(
            text = question,
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 18.sp, lineHeight = 26.sp),
            color = ChagokTheme.colors.ink,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ChagokButton(
            text = yesText,
            onClick = onYesClick,
            modifier = Modifier.weight(YES_WEIGHT),
            height = 60.dp,
            icon = yesIcon,
        )
        ChagokButton(
            text = noText,
            onClick = onNoClick,
            modifier = Modifier.weight(NO_WEIGHT),
            style = ChagokButtonStyle.White,
            height = 60.dp,
        )
    }
    AskHint(text = hint, icon = ChagokIcons.Mic, modifier = Modifier.align(Alignment.CenterHorizontally))
}

private val PanelCardShape = RoundedCornerShape(26.dp)
private val BubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)

// 시안의 버튼 너비: 16 대 10
private const val YES_WEIGHT = 16f
private const val NO_WEIGHT = 10f

@Preview(name = "15 답변 · 찾는 곳 있음", widthDp = 380, heightDp = 605, backgroundColor = 0xFFEEF6F3, showBackground = true)
@Composable
private fun AskPanelFoundPreview() = ChagokTheme {
    AskPanel(
        stage = AskStage.Answer(
            question = "이 근처 치즈케이크 카페 어디였지?",
            kind = AskKind.FIND,
            type = AskAnswerType.FOUND,
            keywordLabel = "치즈케이크 카페",
            memory = AskMemoryUi(RelationUi.COUPLE, "2026.10.12", "10월 12일", "앞산 카페", true, "여기 치즈케이크가 진짜 꾸덕하고 맛있었어"),
            moreCount = 1,
            badSkipped = false,
        ),
        scopeRelation = RelationUi.COUPLE,
        actions = AskActions(),
    )
}
