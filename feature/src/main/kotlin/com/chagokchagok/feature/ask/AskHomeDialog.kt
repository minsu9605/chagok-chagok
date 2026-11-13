package com.chagokchagok.feature.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokDialog
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.domain.ask.AskAnswerType
import com.chagokchagok.core.domain.ask.AskKind
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi

/**
 * 홈에서 물었을 때의 팝업 (화면 19). 듣는 중, 답변, 새 장소 안내를 한 팝업 안에서 보여 준다.
 *
 * @param scopeRelation 지금 기록 중인 관계. 기록 중이 아니면 null이고, 길 안내와 함께 기록도 켜진다고 알린다
 */
@Composable
internal fun AskHomeDialog(
    stage: AskStage,
    scopeRelation: RelationUi?,
    actions: AskActions,
    modifier: Modifier = Modifier,
) {
    ChagokDialog(
        modifier = modifier,
        width = 660.dp,
        onClose = actions.onClose,
        closeContentDescription = stringResource(R.string.feature_ask_close),
    ) {
        ChagokChip(
            text = stringResource(R.string.feature_ask_title),
            colors = ChagokChipDefaults.mint(),
            icon = ChagokIcons.Mic,
            height = 34.dp,
        )
        when (stage) {
            is AskStage.Listening -> {
                AskListeningBody(
                    listening = stage,
                    micSize = 84.dp,
                    onQuestionChange = actions.onQuestionChange,
                    onQuestionSubmit = actions.onQuestionSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AskExampleChip(
                        question = stringResource(R.string.feature_ask_example_cafe_home),
                        containerColor = ChagokTheme.colors.background,
                        onClick = actions.onExampleClick,
                    )
                    AskExampleChip(
                        question = stringResource(R.string.feature_ask_example_visited),
                        containerColor = ChagokTheme.colors.background,
                        onClick = actions.onExampleClick,
                    )
                }
            }
            is AskStage.Answer -> AnswerBody(answer = stage, scopeRelation = scopeRelation, actions = actions)
            is AskStage.Fresh -> {
                AskFreshNotice(modifier = Modifier.fillMaxWidth())
                ChagokButton(
                    text = stringResource(R.string.feature_ask_close),
                    onClick = actions.onClose,
                    modifier = Modifier.fillMaxWidth(),
                    style = ChagokButtonStyle.Secondary,
                    height = 60.dp,
                )
            }
        }
    }
}

@Composable
private fun AnswerBody(
    answer: AskStage.Answer,
    scopeRelation: RelationUi?,
    actions: AskActions,
) {
    val memory = answer.memory
    val recording = scopeRelation != null

    Text(
        text = stringResource(R.string.feature_ask_quote, answer.question),
        modifier = Modifier.padding(end = 56.dp),
        style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 29.sp),
        color = ChagokTheme.colors.ink,
    )
    if (memory == null) {
        AskNoneNotice(answer = answer, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChagokButton(
                text = stringResource(
                    if (answer.kind == AskKind.RECALL) {
                        R.string.feature_ask_yes_recommend
                    } else {
                        R.string.feature_ask_yes_find
                    },
                ),
                onClick = actions.onFreshClick,
                modifier = Modifier.weight(YES_WEIGHT),
                height = 60.dp,
            )
            ChagokButton(
                text = stringResource(R.string.feature_ask_no),
                onClick = actions.onClose,
                modifier = Modifier.weight(NO_WEIGHT),
                style = ChagokButtonStyle.Secondary,
                height = 60.dp,
            )
        }
        return
    }

    val other = answer.type == AskAnswerType.OTHER
    if (other) {
        Text(
            text = stringResource(R.string.feature_ask_other_lead, answer.keywordLabel.orEmpty()),
            style = ChagokTheme.typography.caption,
            color = ChagokTheme.colors.ink,
        )
    }
    AskMemoryPass(memory = memory, detailed = !other)
    if (answer.moreCount > 0) {
        AskMoreChip(moreCount = answer.moreCount, colors = ChagokChipDefaults.background())
    }
    AskHint(
        text = if (recording) scopeText(scopeRelation) else stringResource(R.string.feature_ask_home_scope),
        icon = ChagokIcons.Info,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ChagokButton(
            text = stringResource(if (other) R.string.feature_ask_guide_here else R.string.feature_ask_home_guide),
            onClick = actions.onGuideClick,
            modifier = Modifier.weight(YES_WEIGHT),
            height = 60.dp,
            icon = ChagokIcons.Navigation,
        )
        ChagokButton(
            text = stringResource(if (other) R.string.feature_ask_find_new else R.string.feature_ask_close),
            onClick = if (other) actions.onFreshClick else actions.onClose,
            modifier = Modifier.weight(NO_WEIGHT),
            style = ChagokButtonStyle.Secondary,
            height = 60.dp,
        )
    }
    if (!recording) {
        Text(
            text = stringResource(R.string.feature_ask_home_guide_sub),
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

// 시안의 버튼 너비: 16 대 10
private const val YES_WEIGHT = 16f
private const val NO_WEIGHT = 10f
