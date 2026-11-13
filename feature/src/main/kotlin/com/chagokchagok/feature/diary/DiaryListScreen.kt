package com.chagokchagok.feature.diary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.record.home.MapBackdrop
import com.chagokchagok.feature.record.relation.RelationPickerDialog

/**
 * 일기 보기 (화면 44~47). 왼쪽은 핀 그림, 오른쪽은 일기 카드 목록이고 위의 관계 탭으로 둘을 함께 거른다.
 *
 * @param onRecordStarted 기록 0건 화면에서 기록을 시작했을 때. 값은 묻지 않고 시작했는지 여부
 */
@Composable
fun DiaryListScreen(
    onHomeClick: () -> Unit,
    onDiaryClick: (sessionId: String) -> Unit,
    onRecordStarted: (autoStarted: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnRecordStarted by rememberUpdatedState(onRecordStarted)
    LaunchedEffect(viewModel) {
        viewModel.recordStarted.collect { autoStarted -> currentOnRecordStarted(autoStarted) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        DiaryListContent(
            uiState = uiState,
            onHomeClick = onHomeClick,
            onTabClick = viewModel::onTabClick,
            onDiaryClick = onDiaryClick,
            onStartRecordClick = viewModel::onStartRecordClick,
        )
        val relationPicker = uiState.relationPicker
        if (relationPicker != null) {
            RelationPickerDialog(
                picker = relationPicker,
                onRelationClick = viewModel::onRelationClick,
                onShowAllClick = viewModel::onShowAllRelationsClick,
                onBackToRecommendedClick = viewModel::onBackToRecommendedClick,
                onCloseClick = viewModel::onRelationPickerClose,
            )
        }
    }
}

@Composable
private fun DiaryListContent(
    uiState: DiaryListUiState,
    onHomeClick: () -> Unit,
    onTabClick: (RelationUi?) -> Unit,
    onDiaryClick: (sessionId: String) -> Unit,
    onStartRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiaryPage(modifier = modifier) { compact ->
        DiaryHeader(
            backText = stringResource(R.string.feature_diary_back_home),
            title = stringResource(R.string.feature_diary_title),
            compact = compact,
            onBackClick = onHomeClick,
        ) {
            if (uiState.hasDiaries) {
                RelationTabs(selected = uiState.tab, compact = compact, onTabClick = onTabClick)
            }
        }
        when {
            !uiState.loaded -> Unit
            !uiState.hasDiaries -> EmptyDiary(
                recordingRelation = uiState.recordingRelation,
                compact = compact,
                onStartRecordClick = onStartRecordClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            else -> Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
            ) {
                PinMap(
                    uiState = uiState,
                    onPinClick = onDiaryClick,
                    modifier = Modifier
                        .weight(MAP_WEIGHT)
                        .fillMaxHeight(),
                )
                DiaryCards(
                    uiState = uiState,
                    compact = compact,
                    onDiaryClick = onDiaryClick,
                    modifier = Modifier
                        .weight(LIST_WEIGHT)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

/** 일기 화면들의 바탕과 여백. [content]는 창이 작은지(compact)를 받는다. */
@Composable
internal fun DiaryPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(compact: Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.background),
    ) {
        val compact = maxWidth < COMPACT_MAX_WIDTH
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (compact) 24.dp else 32.dp,
                    vertical = if (compact) 16.dp else 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 18.dp),
        ) {
            content(compact)
        }
    }
}

/** [‹ 뒤로] + 제목 + 오른쪽 끝의 [trailing]. */
@Composable
internal fun DiaryHeader(
    backText: String,
    title: String,
    compact: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChagokButton(
            text = backText,
            onClick = onBackClick,
            style = ChagokButtonStyle.White,
            height = if (compact) 48.dp else 56.dp,
            icon = ChagokIcons.ChevronLeft,
            elevated = true,
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = ChagokTheme.typography.screenTitle.copy(
                fontSize = if (compact) 21.sp else 26.sp,
                lineHeight = if (compact) 28.sp else 34.sp,
            ),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

/** 관계 탭: 전체 · 연인 · 가족 · 친구 · 나 혼자. */
@Composable
private fun RelationTabs(
    selected: RelationUi?,
    compact: Boolean,
    onTabClick: (RelationUi?) -> Unit,
) {
    val pill = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .background(ChagokTheme.colors.fill, pill)
            .padding(4.dp),
    ) {
        TABS.forEach { (relation, labelRes) ->
            val on = relation == selected
            Row(
                modifier = Modifier
                    .height(if (compact) 40.dp else 48.dp)
                    .then(
                        if (on) {
                            Modifier
                                .chagokShadow(ChagokTheme.elevation.level1, pill)
                                .background(ChagokTheme.colors.card, pill)
                        } else {
                            Modifier
                        },
                    )
                    .clip(pill)
                    .clickable(role = Role.Tab) { onTabClick(relation) }
                    .padding(horizontal = if (compact) 13.dp else 20.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (relation != null) RelationDot(relation = relation)
                Text(
                    text = stringResource(labelRes),
                    style = ChagokTheme.typography.label.copy(fontSize = if (compact) 15.sp else 16.sp),
                    color = ChagokTheme.colors.ink,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun RelationDot(relation: RelationUi) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(relation.palette.band, ChagokTheme.shapes.pill),
    )
}

/**
 * 핀 그림. 실제 지도와 실제 좌표가 아니다. 지도와 좌표를 붙이기 전까지 정해 둔 자리에 핀을 찍는다.
 * 핀의 색(관계)과 개수, 범례의 숫자는 실제 기록이다.
 */
@Composable
private fun PinMap(
    uiState: DiaryListUiState,
    onPinClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.clip(ChagokTheme.shapes.alert)) {
        val width = maxWidth
        val height = maxHeight

        MapBackdrop(showPins = false, modifier = Modifier.fillMaxSize())
        uiState.pins.forEach { pin ->
            val position = PIN_POSITIONS[pin.slot % PIN_POSITIONS.size]
            MapPin(
                color = pin.relation.palette.band,
                // 핀의 뾰족한 끝이 그 자리에 오게 놓는다.
                modifier = Modifier
                    .offset(
                        x = width * position.first - PIN_SIZE / 2,
                        y = height * position.second - PIN_SIZE * PIN_TIP_Y,
                    )
                    .clip(ChagokTheme.shapes.pill)
                    .clickable(role = Role.Button) { onPinClick(pin.sessionId) },
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .chagokShadow(ChagokTheme.elevation.level1, ChagokTheme.shapes.pill)
                .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tab = uiState.tab
            if (tab == null) {
                uiState.legend.forEach { (relation, count) ->
                    LegendItem(
                        relation = relation,
                        text = stringResource(
                            R.string.feature_diary_legend_item,
                            stringResource(relation.tabLabelRes),
                            count,
                        ),
                    )
                }
            } else {
                LegendItem(
                    relation = tab,
                    text = stringResource(
                        R.string.feature_diary_legend_filtered,
                        stringResource(tab.withLabelRes),
                        uiState.pins.size,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LegendItem(relation: RelationUi, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RelationDot(relation = relation)
        Text(
            text = text,
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

/** 물방울 모양 핀: 관계 색 바탕, 흰 테두리, 가운데 흰 점. */
@Composable
private fun MapPin(color: Color, modifier: Modifier = Modifier) {
    val path = remember { PathParser().parsePathString(PIN_PATH).toPath() }

    Canvas(modifier = modifier.size(PIN_SIZE)) {
        scale(scale = size.width / PIN_VIEWPORT, pivot = Offset.Zero) {
            drawPath(path = path, color = color)
            drawPath(path = path, color = Color.White, style = Stroke(width = 1.6f))
            drawCircle(color = Color.White, radius = 2.6f, center = Offset(12f, 10f))
        }
    }
}

@Composable
private fun DiaryCards(
    uiState: DiaryListUiState,
    compact: Boolean,
    onDiaryClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tab = uiState.tab
    if (uiState.diaries.isEmpty() && tab != null) {
        Column(
            modifier = modifier.background(ChagokTheme.colors.card, CardShape),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.feature_diary_none_title, stringResource(tab.withLabelRes)),
                style = ChagokTheme.typography.body.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold),
                color = ChagokTheme.colors.ink,
            )
            Text(
                text = stringResource(R.string.feature_diary_none_description),
                style = ChagokTheme.typography.caption,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = uiState.diaries, key = { it.sessionId }) { diary ->
            DiaryCard(diary = diary, compact = compact, onClick = { onDiaryClick(diary.sessionId) })
        }
    }
}

/** 일기 카드: 날짜, 관계 칩, [상세보기], 동선, 칩(별로예요 수, 미입력 한마디 수). */
@Composable
private fun DiaryCard(
    diary: DiaryCardUi,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val relation = diary.relation

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ChagokTheme.colors.card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = diary.dateText,
                style = ChagokTheme.typography.cardTitle.copy(fontSize = if (compact) 19.sp else 21.sp),
                color = ChagokTheme.colors.ink,
                maxLines = 1,
            )
            ChagokChip(
                text = relation.withText(),
                colors = ChagokChipDefaults.relation(relation.palette),
            )
            Box(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .height(42.dp)
                    .background(ChagokTheme.colors.background, ChagokTheme.shapes.pill)
                    .padding(start = 16.dp, end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.feature_diary_open_detail),
                    style = ChagokTheme.typography.label.copy(fontSize = 15.sp),
                    color = ChagokTheme.colors.ink,
                    maxLines = 1,
                )
                Icon(
                    imageVector = ChagokIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ChagokTheme.colors.ink,
                )
            }
        }
        Text(
            text = diary.placeNames.joinToString(separator = " → "),
            style = ChagokTheme.typography.caption.copy(fontSize = if (compact) 15.sp else 16.sp),
            color = ChagokTheme.colors.inkSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (diary.badCount > 0 || diary.missingMemoCount > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (diary.badCount > 0) {
                    ChagokChip(
                        text = stringResource(R.string.feature_diary_bad_count, diary.badCount),
                        colors = ChagokChipDefaults.fill(),
                        icon = ChagokIcons.ThumbDown,
                    )
                }
                if (diary.missingMemoCount > 0) {
                    ChagokChip(
                        text = stringResource(R.string.feature_diary_missing_memo, diary.missingMemoCount),
                        colors = ChagokChipDefaults.danger(),
                        icon = ChagokIcons.Alert,
                    )
                }
            }
        }
    }
}

/** 기록 0건 (화면 46), 0건인데 첫 기록 진행 중 (화면 47). */
@Composable
private fun EmptyDiary(
    recordingRelation: RelationUi?,
    compact: Boolean,
    onStartRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(ChagokTheme.colors.card, ChagokTheme.shapes.widget),
        verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 22.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyDiaryArt()
        if (recordingRelation != null) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .background(recordingRelation.palette.soft, ChagokTheme.shapes.pill)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RelationDot(relation = recordingRelation)
                Text(
                    text = stringResource(
                        R.string.feature_diary_empty_recording_chip,
                        stringResource(recordingRelation.modeLabelRes),
                    ),
                    style = ChagokTheme.typography.label.copy(fontSize = 15.sp),
                    color = recordingRelation.palette.ink,
                )
            }
            EmptyTitle(text = stringResource(R.string.feature_diary_empty_recording_title), compact = compact)
            Text(
                text = stringResource(R.string.feature_diary_empty_recording_description),
                style = ChagokTheme.typography.caption.copy(fontSize = 17.sp),
                color = ChagokTheme.colors.inkSecondary,
            )
        } else {
            EmptyTitle(text = stringResource(R.string.feature_diary_empty_title), compact = compact)
            ChagokButton(
                text = stringResource(R.string.feature_diary_empty_start),
                onClick = onStartRecordClick,
                height = if (compact) 64.dp else 72.dp,
                icon = ChagokIcons.Plus,
                elevated = true,
            )
        }
    }
}

@Composable
private fun EmptyTitle(text: String, compact: Boolean) {
    Text(
        text = text,
        style = ChagokTheme.typography.screenTitle.copy(
            fontSize = if (compact) 22.sp else 26.sp,
            lineHeight = if (compact) 32.sp else 38.sp,
        ),
        color = ChagokTheme.colors.ink,
        textAlign = TextAlign.Center,
    )
}

/** 빈 일기장 그림: 쌓인 카드 3장. 맨 앞 카드에 더하기 표시가 있다. */
@Composable
private fun EmptyDiaryArt() {
    Box(modifier = Modifier.size(width = 176.dp, height = 150.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 112.dp, height = 70.dp)
                .background(ChagokTheme.colors.background, RoundedCornerShape(20.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 36.dp)
                .size(width = 144.dp, height = 70.dp)
                .background(ChagokTheme.colors.mintSoft, RoundedCornerShape(22.dp)),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(78.dp)
                .background(ChagokTheme.colors.mint, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ChagokTheme.colors.mintInk,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 8.dp)
                        .background(ChagokTheme.colors.mintInk, ChagokTheme.shapes.pill),
                )
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 8.dp)
                        .background(ChagokTheme.colors.mintInk.copy(alpha = 0.5f), ChagokTheme.shapes.pill),
                )
            }
        }
    }
}

/** 탭과 범례에 쓰는 짧은 관계 이름. 예: "연인" */
internal val RelationUi.tabLabelRes: Int
    get() = when (this) {
        RelationUi.SOLO -> R.string.feature_diary_tab_solo
        RelationUi.COUPLE -> R.string.feature_diary_tab_couple
        RelationUi.FRIEND -> R.string.feature_diary_tab_friend
        RelationUi.FAMILY -> R.string.feature_diary_tab_family
    }

/** "연인과 함께" · "나 혼자" */
@Composable
internal fun RelationUi.withText(): String =
    if (this == RelationUi.SOLO) {
        stringResource(R.string.feature_diary_with_solo)
    } else {
        stringResource(R.string.feature_diary_with, stringResource(withLabelRes))
    }

internal val COMPACT_MAX_WIDTH = 1000.dp
internal val CardShape = RoundedCornerShape(26.dp)

// 시안의 열 너비: 핀 그림 560, 나머지가 일기 카드
private const val MAP_WEIGHT = 560f
private const val LIST_WEIGHT = 636f

private val TABS: List<Pair<RelationUi?, Int>> = listOf(
    null to R.string.feature_diary_tab_all,
    RelationUi.COUPLE to R.string.feature_diary_tab_couple,
    RelationUi.FAMILY to R.string.feature_diary_tab_family,
    RelationUi.FRIEND to R.string.feature_diary_tab_friend,
    RelationUi.SOLO to R.string.feature_diary_tab_solo,
)

// 핀 모양 (24×24 격자). 뾰족한 끝은 (12, 22)에 있다.
private const val PIN_PATH = "M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0Z"
private const val PIN_VIEWPORT = 24f
private const val PIN_TIP_Y = 22f / 24f
private val PIN_SIZE = 44.dp

// 핀을 놓는 자리 (그림 크기에 대한 비율). 실제 좌표가 아니다.
private val PIN_POSITIONS = listOf(
    0.46f to 0.44f,
    0.76f to 0.54f,
    0.34f to 0.64f,
    0.58f to 0.70f,
    0.84f to 0.24f,
    0.92f to 0.38f,
    0.66f to 0.32f,
    0.40f to 0.82f,
    0.74f to 0.80f,
    0.22f to 0.36f,
    0.56f to 0.18f,
    0.88f to 0.68f,
)

@Preview(name = "44 일기 보기 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DiaryListPreview() = ChagokTheme {
    DiaryListContent(
        uiState = DiaryListUiState(
            loaded = true,
            hasDiaries = true,
            diaries = listOf(
                DiaryCardUi("1", "2026.11.09", RelationUi.COUPLE, listOf("교동 뭉티기", "앞산 카페", "수성못 산책로"), 0, 1),
                DiaryCardUi("2", "2026.10.24", RelationUi.FAMILY, listOf("초당순두부", "강릉 테라로사"), 1, 0),
            ),
            pins = listOf(
                DiaryPinUi("a", RelationUi.COUPLE, 0, "1"),
                DiaryPinUi("b", RelationUi.COUPLE, 1, "1"),
                DiaryPinUi("c", RelationUi.FAMILY, 4, "2"),
            ),
            legend = listOf(RelationUi.COUPLE to 2, RelationUi.FAMILY to 1),
        ),
        onHomeClick = {},
        onTabClick = {},
        onDiaryClick = {},
        onStartRecordClick = {},
    )
}

@Preview(name = "46 기록 0건 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DiaryEmptyPreview() = ChagokTheme {
    DiaryListContent(
        uiState = DiaryListUiState(loaded = true),
        onHomeClick = {},
        onTabClick = {},
        onDiaryClick = {},
        onStartRecordClick = {},
    )
}
