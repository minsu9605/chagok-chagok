package com.chagokchagok.feature.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.common.rememberKeyboardAllowed
import com.chagokchagok.feature.diary.DiaryHeader
import com.chagokchagok.feature.diary.DiaryPage
import com.chagokchagok.feature.diary.tabLabelRes

/**
 * 코스 짜기 (화면 36~43). 어디로 갈지 듣고, 내 기억에서 찾아 2~3번 묻고 답한 뒤 코스 카드를 보여 준다.
 *
 * @param onHomeClick [메인 홈으로]
 * @param onDeparted [이 코스로 출발하기]로 출발했을 때. 값은 새 기록을 시작했는지 여부
 */
@Composable
fun CourseScreen(
    onHomeClick: () -> Unit,
    onDeparted: (newRecord: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDeparted by rememberUpdatedState(onDeparted)
    LaunchedEffect(viewModel) {
        viewModel.departed.collect { newRecord -> currentOnDeparted(newRecord) }
    }

    CourseContent(
        uiState = uiState,
        actions = CourseActions(
            onHomeClick = onHomeClick,
            onTextChange = viewModel::onTextChange,
            onTextSubmit = viewModel::onTextSubmit,
            onQuickStartClick = viewModel::onQuickStartClick,
            onRestartClick = viewModel::onRestartClick,
            onFirstPick = viewModel::onFirstPick,
            onKeepPick = viewModel::onKeepPick,
            onCandidatePick = viewModel::onCandidatePick,
            onSuggestNewClick = viewModel::onSuggestNewClick,
            onThemePick = viewModel::onThemePick,
            onSwapClick = viewModel::onSwapClick,
            onSwapPick = viewModel::onSwapPick,
            onSwapClose = viewModel::onSwapClose,
            onDepartClick = viewModel::onDepartClick,
        ),
        modifier = modifier,
    )
}

/** 코스 짜기에서 일어나는 동작 묶음. */
internal data class CourseActions(
    val onHomeClick: () -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    val onTextSubmit: () -> Unit = {},
    val onQuickStartClick: (String) -> Unit = {},
    val onRestartClick: () -> Unit = {},
    val onFirstPick: () -> Unit = {},
    val onKeepPick: (keepMemory: Boolean) -> Unit = {},
    val onCandidatePick: (CourseItemUi) -> Unit = {},
    val onSuggestNewClick: () -> Unit = {},
    val onThemePick: (CourseThemeUi) -> Unit = {},
    val onSwapClick: (index: Int) -> Unit = {},
    val onSwapPick: (CourseItemUi) -> Unit = {},
    val onSwapClose: () -> Unit = {},
    val onDepartClick: () -> Unit = {},
)

@Composable
private fun CourseContent(
    uiState: CourseUiState,
    actions: CourseActions,
    modifier: Modifier = Modifier,
) {
    if (uiState.step == CourseStep.Card) {
        CourseCardContent(uiState = uiState, actions = actions, modifier = modifier)
        return
    }
    DiaryPage(modifier = modifier) { compact ->
        DiaryHeader(
            backText = stringResource(R.string.feature_diary_back_home),
            title = "",
            compact = compact,
            onBackClick = actions.onHomeClick,
        ) {
            CourseBadge(relation = uiState.relation, live = uiState.listening)
            if (uiState.questionCount > 0) {
                ChagokChip(
                    text = stringResource(R.string.feature_course_question_count, uiState.questionCount),
                    colors = ChagokChipDefaults.fill(),
                    height = 40.dp,
                )
            }
        }
        when (val step = uiState.step) {
            CourseStep.Start -> StartBody(uiState = uiState, compact = compact, actions = actions)
            else -> ChatBody(relation = uiState.relation, step = step, actions = actions)
        }
    }
}

/** "● 연인 맞춤 코스 짜기" */
@Composable
private fun CourseBadge(relation: RelationUi, live: Boolean) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    if (live) ChagokTheme.colors.mint else ChagokTheme.colors.fill,
                    ChagokTheme.shapes.pill,
                ),
        )
        Text(
            text = stringResource(R.string.feature_course_badge, stringResource(relation.tabLabelRes)),
            style = ChagokTheme.typography.label.copy(fontSize = 15.sp),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

/** 시작 (화면 36): 왼쪽에 물음과 듣는 중, 오른쪽에 눌러서 시작하는 추천 칩. */
@Composable
private fun ColumnScope.StartBody(
    uiState: CourseUiState,
    compact: Boolean,
    actions: CourseActions,
) {
    val relation = uiState.relation
    val circle = ChagokTheme.shapes.pill
    val keyboardAllowed = rememberKeyboardAllowed()

    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(START_WEIGHT)
                .fillMaxHeight()
                .background(ChagokTheme.colors.card, ChagokTheme.shapes.widget)
                .padding(if (compact) 24.dp else 32.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 22.dp, Alignment.CenterVertically),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ChagokTheme.colors.mint, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Logo,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
            Text(
                text = if (relation == RelationUi.SOLO) {
                    stringResource(R.string.feature_course_start_title_solo)
                } else {
                    stringResource(R.string.feature_course_start_title, stringResource(relation.withLabelRes))
                },
                style = ChagokTheme.typography.screenTitle.copy(
                    fontSize = if (compact) 21.sp else 28.sp,
                    lineHeight = if (compact) 31.sp else 42.sp,
                ),
                color = ChagokTheme.colors.ink,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(ChagokTheme.colors.mintSoft, circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .chagokShadow(ChagokTheme.elevation.level2, circle)
                            .background(ChagokTheme.colors.mint, circle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = ChagokIcons.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = ChagokTheme.colors.onMint,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(
                            when {
                                uiState.listening -> R.string.feature_course_listening
                                !keyboardAllowed -> R.string.feature_keyboard_blocked
                                else -> R.string.feature_ask_typing
                            },
                        ),
                        style = ChagokTheme.typography.label.copy(fontSize = 16.sp),
                        color = ChagokTheme.colors.mintInk,
                    )
                    Box {
                        if (uiState.heardText.isEmpty() && (uiState.listening || keyboardAllowed)) {
                            Text(
                                text = stringResource(
                                    if (uiState.listening) {
                                        R.string.feature_course_start_example
                                    } else {
                                        R.string.feature_ask_placeholder_typing
                                    },
                                ),
                                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp),
                                color = ChagokTheme.colors.inkSecondary,
                            )
                        }
                        BasicTextField(
                            value = uiState.heardText,
                            onValueChange = actions.onTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = keyboardAllowed,
                            textStyle = ChagokTheme.typography.caption.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChagokTheme.colors.ink,
                            ),
                            cursorBrush = SolidColor(ChagokTheme.colors.mint),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { actions.onTextSubmit() }),
                            maxLines = 2,
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(QUICK_WEIGHT)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_course_quick_title),
                modifier = Modifier.padding(start = 6.dp),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.inkSecondary,
            )
            val relationColors = ChagokTheme.colors.relation
            QuickStartCard(
                labelRes = R.string.feature_course_quick_sea,
                utteranceRes = R.string.feature_course_quick_sea_utterance,
                icon = ChagokIcons.Waves,
                soft = relationColors.friend.soft,
                deep = relationColors.friend.ink,
                onClick = actions.onQuickStartClick,
            )
            QuickStartCard(
                labelRes = R.string.feature_course_quick_dessert,
                utteranceRes = R.string.feature_course_quick_dessert_utterance,
                icon = ChagokIcons.Coffee,
                soft = relationColors.couple.soft,
                deep = relationColors.couple.ink,
                onClick = actions.onQuickStartClick,
            )
            QuickStartCard(
                labelRes = R.string.feature_course_quick_soup,
                utteranceRes = R.string.feature_course_quick_soup_utterance,
                icon = ChagokIcons.Bowl,
                soft = relationColors.family.soft,
                deep = relationColors.family.ink,
                onClick = actions.onQuickStartClick,
            )
            QuickStartCard(
                labelRes = R.string.feature_course_quick_near,
                utteranceRes = R.string.feature_course_quick_near_utterance,
                icon = ChagokIcons.Pin,
                soft = ChagokTheme.colors.mintSoft,
                deep = ChagokTheme.colors.mintInk,
                onClick = actions.onQuickStartClick,
            )
        }
    }
}

/** 추천 칩 하나. 누르면 [utteranceRes]의 말을 한 것처럼 시작한다. */
@Composable
private fun ColumnScope.QuickStartCard(
    labelRes: Int,
    utteranceRes: Int,
    icon: ImageVector,
    soft: Color,
    deep: Color,
    onClick: (String) -> Unit,
) {
    val utterance = stringResource(utteranceRes)
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(shape)
            .background(ChagokTheme.colors.card)
            .clickable(role = Role.Button) { onClick(utterance) }
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(soft, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = deep)
        }
        Text(
            text = stringResource(labelRes),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 17.sp, lineHeight = 24.sp),
            color = ChagokTheme.colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 묻고 답하기 (화면 37~41): 내 말, 앱의 답, 고를 버튼, 아래 듣는 중 알약. */
@Composable
private fun ColumnScope.ChatBody(
    relation: RelationUi,
    step: CourseStep,
    actions: CourseActions,
) {
    // "두 분이 좋았다고…"의 앞말. 나 혼자는 비운다. 문자열 리소스는 끝의 빈칸을 지우므로 빈칸은 여기서 붙인다.
    val who = when (relation) {
        RelationUi.COUPLE -> stringResource(R.string.feature_course_who_couple) + " "
        RelationUi.FRIEND -> stringResource(R.string.feature_course_who_friend) + " "
        RelationUi.FAMILY -> stringResource(R.string.feature_course_who_family) + " "
        RelationUi.SOLO -> ""
    }
    val utterance: String
    val message: String
    val hint: String
    when (step) {
        is CourseStep.CaseA -> {
            utterance = step.utterance
            message = if (step.keywordLabel == null) {
                stringResource(R.string.feature_course_a_message, who, step.memoryCount)
            } else {
                stringResource(R.string.feature_course_a_message_keyword, step.keywordLabel, who, step.memoryCount)
            }
            hint = stringResource(R.string.feature_course_a_hint)
        }
        is CourseStep.CaseB -> {
            utterance = step.utterance
            message = stringResource(R.string.feature_course_b_message, step.keywordLabel, step.memory.name)
            hint = stringResource(R.string.feature_course_b_hint)
        }
        is CourseStep.NewCandidates -> {
            utterance = step.utterance
            val (first, second) = step.candidates
            message = stringResource(
                R.string.feature_course_candidates_message,
                first.name,
                first.distanceText.orEmpty(),
                second.name,
                second.distanceText.orEmpty(),
            )
            hint = stringResource(R.string.feature_course_candidates_hint)
        }
        is CourseStep.CaseC -> {
            utterance = step.utterance
            message = stringResource(R.string.feature_course_c_message)
            hint = stringResource(R.string.feature_course_c_hint)
        }
        is CourseStep.Themes -> {
            utterance = step.utterance
            val (first, second) = step.themes
            message = stringResource(R.string.feature_course_themes_message, first.title, second.title)
            hint = stringResource(R.string.feature_course_themes_hint, first.title)
        }
        CourseStep.Start, CourseStep.Card -> return
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), reverseScrolling = true),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
    ) {
        Text(
            text = utterance,
            modifier = Modifier
                .align(Alignment.End)
                .widthIn(max = 520.dp)
                .background(ChagokTheme.colors.mintSoft, UserBubbleShape)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            style = ChagokTheme.typography.caption.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(LOGO_SIZE)
                    .background(ChagokTheme.colors.mint, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Logo,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
            Text(
                text = message,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .background(ChagokTheme.colors.card, AppBubbleShape)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                style = ChagokTheme.typography.caption.copy(fontSize = 17.sp, lineHeight = 26.sp),
                color = ChagokTheme.colors.ink,
            )
        }
        Column(
            modifier = Modifier.padding(start = LOGO_SIZE + 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChatChoices(relation = relation, step = step, actions = actions)
        }
    }
    // 말로 고르는 것은 아직 연결하지 않았다. 시안대로 알약만 보여 준다.
    Row(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .chagokShadow(ChagokTheme.elevation.level2, ChagokTheme.shapes.pill)
            .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill)
            .padding(start = 8.dp, top = 8.dp, end = 22.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(ChagokTheme.colors.mint, ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ChagokIcons.Mic,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = ChagokTheme.colors.onMint,
            )
        }
        Text(
            text = stringResource(R.string.feature_course_listening_short),
            style = ChagokTheme.typography.label.copy(fontSize = 15.sp),
            color = ChagokTheme.colors.mintInk,
        )
        Text(
            text = hint,
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
            color = ChagokTheme.colors.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 단계마다 다른 고를 것들. */
@Composable
private fun ChatChoices(
    relation: RelationUi,
    step: CourseStep,
    actions: CourseActions,
) {
    when (step) {
        is CourseStep.CaseA -> ChoiceRow {
            ChagokButton(
                text = stringResource(R.string.feature_course_a_meal),
                onClick = actions.onFirstPick,
                height = CHOICE_HEIGHT,
                icon = ChagokIcons.Bowl,
                elevated = true,
            )
            ChagokButton(
                text = stringResource(R.string.feature_course_a_cafe),
                onClick = actions.onFirstPick,
                style = ChagokButtonStyle.White,
                height = CHOICE_HEIGHT,
                icon = ChagokIcons.Coffee,
            )
        }
        is CourseStep.CaseB -> {
            Row(
                modifier = Modifier
                    .background(relation.palette.soft, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = relation.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = relation.palette.ink,
                    )
                }
                Column {
                    Text(
                        text = step.memory.name,
                        style = ChagokTheme.typography.cardTitle.copy(fontSize = 18.sp, lineHeight = 24.sp),
                        color = ChagokTheme.colors.ink,
                    )
                    Text(
                        text = stringResource(
                            R.string.feature_course_b_memory_sub,
                            stringResource(
                                if (relation == RelationUi.SOLO) {
                                    R.string.feature_diary_with_solo
                                } else {
                                    relation.withLabelRes
                                },
                            ),
                            step.memory.dateText.orEmpty(),
                        ),
                        style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                        color = ChagokTheme.colors.inkSecondary,
                    )
                }
            }
            ChoiceRow {
                ChagokButton(
                    text = stringResource(R.string.feature_course_b_keep, step.memory.name),
                    onClick = { actions.onKeepPick(true) },
                    height = CHOICE_HEIGHT,
                    elevated = true,
                )
                ChagokButton(
                    text = stringResource(R.string.feature_course_b_all_new),
                    onClick = { actions.onKeepPick(false) },
                    style = ChagokButtonStyle.White,
                    height = CHOICE_HEIGHT,
                    icon = ChagokIcons.Search,
                )
            }
        }
        is CourseStep.NewCandidates -> {
            ChoiceRow {
                step.candidates.forEachIndexed { index, candidate ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ChagokTheme.colors.card)
                            .clickable(role = Role.Button) { actions.onCandidatePick(candidate) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OrderCircle(number = index + 1, first = index == 0)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = candidate.name,
                                style = ChagokTheme.typography.cardTitle.copy(fontSize = 18.sp, lineHeight = 24.sp),
                                color = ChagokTheme.colors.ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(
                                    R.string.feature_course_new_place_sub,
                                    candidate.category.orEmpty(),
                                    candidate.distanceText.orEmpty(),
                                ),
                                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                                color = ChagokTheme.colors.inkSecondary,
                            )
                        }
                        ChagokChip(
                            text = stringResource(R.string.feature_course_new_place),
                            colors = ChagokChipDefaults.fill(),
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.feature_course_candidates_note),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        is CourseStep.CaseC -> ChoiceRow {
            ChagokButton(
                text = stringResource(R.string.feature_course_c_yes),
                onClick = actions.onSuggestNewClick,
                height = CHOICE_HEIGHT,
                elevated = true,
            )
            ChagokButton(
                text = stringResource(R.string.feature_course_c_change),
                onClick = actions.onRestartClick,
                style = ChagokButtonStyle.White,
                height = CHOICE_HEIGHT,
                icon = ChagokIcons.Refresh,
            )
        }
        is CourseStep.Themes -> ChoiceRow {
            step.themes.forEachIndexed { index, theme ->
                ChagokButton(
                    text = stringResource(R.string.feature_course_theme_label, theme.title, theme.flow),
                    onClick = { actions.onThemePick(theme) },
                    style = if (index == 0) ChagokButtonStyle.Primary else ChagokButtonStyle.White,
                    height = CHOICE_HEIGHT,
                    icon = if (index == 0) ChagokIcons.Bowl else ChagokIcons.Coffee,
                    elevated = index == 0,
                )
            }
        }
        CourseStep.Start, CourseStep.Card -> Unit
    }
}

@Composable
private fun ChoiceRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** 순서 동그라미. 첫 번째는 민트로 채운다. */
@Composable
internal fun OrderCircle(number: Int, first: Boolean) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                if (first) ChagokTheme.colors.mint else ChagokTheme.colors.mintSoft,
                ChagokTheme.shapes.pill,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = ChagokTheme.typography.label.copy(fontSize = 16.sp),
            color = ChagokTheme.colors.onMint,
        )
    }
}

private val UserBubbleShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 6.dp, bottomStart = 22.dp)
private val AppBubbleShape = RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 22.dp)
private val LOGO_SIZE = 44.dp
private val CHOICE_HEIGHT = 60.dp

// 시안의 열 너비: 물음 676, 추천 칩 520
private const val START_WEIGHT = 676f
private const val QUICK_WEIGHT = 520f

@Preview(name = "36 코스 시작 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun CourseStartPreview() = ChagokTheme {
    CourseContent(
        uiState = CourseUiState(relation = RelationUi.COUPLE, listening = true),
        actions = CourseActions(),
    )
}

@Preview(name = "37 Case A · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun CourseCaseAPreview() = ChagokTheme {
    CourseContent(
        uiState = CourseUiState(
            relation = RelationUi.COUPLE,
            step = CourseStep.CaseA("오늘 강릉 갈 건데 데이트 코스 좀 짜줘.", "강릉", 4),
            questionCount = 1,
        ),
        actions = CourseActions(),
    )
}
