package com.chagokchagok.feature.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.objectParticle
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.record.complete.RouteMap
import com.chagokchagok.feature.record.memory.MemoDialog
import com.chagokchagok.feature.record.relation.RelationPickerDialog
import com.chagokchagok.feature.record.relation.RelationPickerGuide

/**
 * 일기 상세 (화면 48~55). 왼쪽은 그날 동선 그림, 오른쪽은 장소 카드다.
 * 한마디 추가·다시 말하기, 호불호 변경, 장소·일기 삭제, 그 코스로 다시 출발을 한다.
 *
 * @param onBackClick [목록으로], 일기를 지운 뒤
 * @param onDeparted 다시 출발했을 때. 새 기록을 시작했는지, 묻지 않고 시작했는지를 넘긴다
 */
@Composable
fun DiaryDetailScreen(
    onBackClick: () -> Unit,
    onDeparted: (newRecord: Boolean, autoStarted: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBackClick by rememberUpdatedState(onBackClick)
    val currentOnDeparted by rememberUpdatedState(onDeparted)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DiaryDetailEvent.DiaryDeleted -> currentOnBackClick()
                is DiaryDetailEvent.Departed -> currentOnDeparted(event.newRecord, event.autoStarted)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val stopPicker = uiState.stopPicker
        if (stopPicker != null) {
            PickStopsContent(
                uiState = uiState,
                picker = stopPicker,
                onBackClick = onBackClick,
                onStopClick = viewModel::onStopClick,
                onConfirmClick = viewModel::onStopsConfirm,
            )
        } else {
            DiaryDetailContent(
                uiState = uiState,
                actions = DetailActions(
                    onBackClick = onBackClick,
                    onDeleteDiaryClick = viewModel::onDeleteDiaryClick,
                    onAddMemoClick = viewModel::onAddMemoClick,
                    onRespeakClick = viewModel::onRespeakClick,
                    onRatingMenuClick = viewModel::onRatingMenuClick,
                    onRatingMenuDismiss = viewModel::onRatingMenuDismiss,
                    onRatingPick = viewModel::onRatingPick,
                    onDeleteVisitClick = viewModel::onDeleteVisitClick,
                    onSpotGuideClick = viewModel::onSpotGuideClick,
                    onRestartClick = viewModel::onRestartClick,
                ),
            )
        }

        val memoEditing = uiState.memoEditing
        val respeak = uiState.respeak
        val deletingVisit = uiState.deletingVisit
        val restartPicker = uiState.restartPicker
        when {
            memoEditing != null -> MemoDialog(
                memo = memoEditing,
                onTextChange = viewModel::onMemoTextChange,
                onSaveClick = viewModel::onMemoSaveClick,
                onLaterClick = viewModel::onMemoLaterClick,
                hint = stringResource(R.string.feature_diary_memo_hint),
            )
            respeak != null -> RespeakDialog(
                respeak = respeak,
                monthDayText = uiState.monthDayText,
                onTextChange = viewModel::onRespeakTextChange,
                onSaveClick = viewModel::onRespeakSaveClick,
                onCloseClick = viewModel::onRespeakClose,
            )
            deletingVisit != null -> DiaryAlert(
                title = stringResource(
                    R.string.feature_diary_delete_place_title,
                    deletingVisit.placeName,
                    objectParticle(deletingVisit.placeName),
                ),
                description = stringResource(R.string.feature_diary_delete_place_description),
                confirmText = stringResource(R.string.feature_diary_delete),
                onCancelClick = viewModel::onDeleteCancel,
                onConfirmClick = viewModel::onDeleteVisitConfirm,
            )
            uiState.deletingDiary -> DiaryAlert(
                title = stringResource(R.string.feature_diary_delete_diary_title, uiState.monthDayText),
                description = stringResource(R.string.feature_diary_delete_diary_description, uiState.visits.size),
                confirmText = stringResource(R.string.feature_diary_delete_diary),
                onCancelClick = viewModel::onDeleteCancel,
                onConfirmClick = viewModel::onDeleteDiaryConfirm,
            )
            restartPicker != null -> RelationPickerDialog(
                picker = restartPicker,
                onRelationClick = viewModel::onRestartRelationClick,
                onShowAllClick = {},
                onBackToRecommendedClick = {},
                onCloseClick = viewModel::onRestartPickerClose,
                guide = RelationPickerGuide(
                    chipText = stringResource(R.string.feature_diary_restart_chip),
                    noteText = stringResource(R.string.feature_diary_restart_note),
                ),
            )
        }
    }
}

/** 상세 화면에서 일어나는 동작 묶음. */
private data class DetailActions(
    val onBackClick: () -> Unit = {},
    val onDeleteDiaryClick: () -> Unit = {},
    val onAddMemoClick: (visitId: String, label: String) -> Unit = { _, _ -> },
    val onRespeakClick: (visitId: String) -> Unit = {},
    val onRatingMenuClick: (visitId: String) -> Unit = {},
    val onRatingMenuDismiss: () -> Unit = {},
    val onRatingPick: (good: Boolean) -> Unit = {},
    val onDeleteVisitClick: (visitId: String) -> Unit = {},
    val onSpotGuideClick: () -> Unit = {},
    val onRestartClick: () -> Unit = {},
)

@Composable
private fun DiaryDetailContent(
    uiState: DiaryDetailUiState,
    actions: DetailActions,
    modifier: Modifier = Modifier,
) {
    val relation = uiState.relation

    DiaryPage(modifier = modifier) { compact ->
        DiaryHeader(
            backText = stringResource(R.string.feature_diary_back_list),
            title = if (relation == null) "" else diaryTitle(dateText = uiState.dateText, relation = relation),
            compact = compact,
            onBackClick = actions.onBackClick,
        ) {
            if (relation != null) {
                DeleteDiaryButton(compact = compact, onClick = actions.onDeleteDiaryClick)
            }
        }
        if (relation != null) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
            ) {
                RouteMap(
                    relation = relation,
                    placeNames = uiState.visits.map { it.placeName },
                    modifier = Modifier
                        .weight(MAP_WEIGHT)
                        .fillMaxHeight(),
                    labelRes = R.string.feature_diary_route,
                )
                Column(
                    modifier = Modifier
                        .weight(LIST_WEIGHT)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(items = uiState.visits, key = { it.visitId }) { visit ->
                            val label = visitLabel(visit.number, visit.placeName, uiState.monthDayText)
                            DetailVisitCard(
                                relation = relation,
                                visit = visit,
                                ratingMenuOpen = uiState.ratingMenuVisitId == visit.visitId,
                                onAddMemoClick = { actions.onAddMemoClick(visit.visitId, label) },
                                onRespeakClick = { actions.onRespeakClick(visit.visitId) },
                                onRatingMenuClick = { actions.onRatingMenuClick(visit.visitId) },
                                onRatingMenuDismiss = actions.onRatingMenuDismiss,
                                onRatingPick = actions.onRatingPick,
                                onDeleteClick = { actions.onDeleteVisitClick(visit.visitId) },
                                onSpotGuideClick = actions.onSpotGuideClick,
                            )
                        }
                    }
                    RestartButton(
                        recording = uiState.recording,
                        compact = compact,
                        onClick = actions.onRestartClick,
                    )
                }
            }
        }
    }
}

/** 흰 바탕에 빨간 글자의 [일기 삭제]. */
@Composable
private fun DeleteDiaryButton(compact: Boolean, onClick: () -> Unit) {
    val pill = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .height(if (compact) 48.dp else 52.dp)
            .chagokShadow(ChagokTheme.elevation.level1, pill)
            .clip(pill)
            .background(ChagokTheme.colors.card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ChagokIcons.Trash,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ChagokTheme.colors.danger,
        )
        Text(
            text = stringResource(R.string.feature_diary_delete_diary),
            style = ChagokTheme.typography.label.copy(fontSize = if (compact) 16.sp else 17.sp),
            color = ChagokTheme.colors.danger,
            maxLines = 1,
        )
    }
}

/** 장소 카드: 순서, 이름, 호불호 버튼, 휴지통, 한마디와 그 아래 동작들. */
@Composable
private fun DetailVisitCard(
    relation: RelationUi,
    visit: DetailVisitUi,
    ratingMenuOpen: Boolean,
    onAddMemoClick: () -> Unit,
    onRespeakClick: () -> Unit,
    onRatingMenuClick: () -> Unit,
    onRatingMenuDismiss: () -> Unit,
    onRatingPick: (good: Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onSpotGuideClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, CardShape)
            .padding(start = 18.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(relation.palette.band, ChagokTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = visit.number.toString(),
                    style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
            }
            Text(
                text = visit.placeName,
                modifier = Modifier.weight(1f),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 20.sp, lineHeight = 28.sp),
                color = ChagokTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box {
                RatingButton(good = visit.good, onClick = onRatingMenuClick)
                if (ratingMenuOpen) {
                    val density = LocalDensity.current
                    val offsetY = with(density) { (RATING_BUTTON_HEIGHT + 8.dp).roundToPx() }
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(x = 0, y = offsetY),
                        onDismissRequest = onRatingMenuDismiss,
                        properties = PopupProperties(focusable = true),
                    ) {
                        // 팝업은 따로 뜨는 창이라 앱에서 맞춘 화면 배율을 잃는다. 바깥의 배율을 다시 넘겨준다.
                        CompositionLocalProvider(LocalDensity provides density) {
                            RatingMenu(good = visit.good, onPick = onRatingPick)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(RATING_BUTTON_HEIGHT)
                    .clip(ChagokTheme.shapes.pill)
                    .background(ChagokTheme.colors.background)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.feature_diary_delete_place),
                        onClick = onDeleteClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Trash,
                    contentDescription = stringResource(R.string.feature_diary_delete_place),
                    modifier = Modifier.size(18.dp),
                    tint = ChagokTheme.colors.inkSecondary,
                )
            }
        }
        if (visit.memo != null) {
            // 해시태그는 한마디를 정리해 주는 연결이 붙어야 생긴다. 그때까지는 이 안내를 보여 준다.
            ChagokChip(
                text = stringResource(R.string.feature_diary_tag_pending),
                colors = ChagokChipDefaults.fill(),
                icon = ChagokIcons.WifiOff,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (visit.memo != null) {
                Text(
                    text = stringResource(R.string.feature_memory_saved_memo_quote, visit.memo),
                    modifier = Modifier.weight(1f),
                    style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ChagokTheme.colors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                CardLink(
                    text = stringResource(R.string.feature_diary_respeak),
                    icon = ChagokIcons.Refresh,
                    onClick = onRespeakClick,
                )
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = ChagokIcons.Alert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ChagokTheme.colors.onLemon,
                    )
                    Text(
                        text = stringResource(R.string.feature_complete_no_memo),
                        style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = ChagokTheme.colors.onLemon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CardLink(
                    text = stringResource(R.string.feature_diary_add_memo),
                    icon = ChagokIcons.Mic,
                    onClick = onAddMemoClick,
                    soft = true,
                )
            }
            CardLink(
                text = stringResource(R.string.feature_diary_spot_guide),
                icon = ChagokIcons.Pin,
                onClick = onSpotGuideClick,
            )
        }
    }
}

/** [👍 좋았어요 ▾] */
@Composable
private fun RatingButton(good: Boolean, onClick: () -> Unit) {
    val content = if (good) ChagokTheme.colors.onMintSoft else ChagokTheme.colors.ink

    Row(
        modifier = Modifier
            .height(RATING_BUTTON_HEIGHT)
            .clip(ChagokTheme.shapes.pill)
            .background(if (good) ChagokTheme.colors.mintSoft else ChagokTheme.colors.fill)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.feature_diary_change_rating),
                onClick = onClick,
            )
            .padding(start = 14.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (good) ChagokIcons.ThumbUp else ChagokIcons.ThumbDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = content,
        )
        Text(
            text = stringResource(if (good) R.string.feature_memory_good else R.string.feature_memory_bad),
            style = ChagokTheme.typography.label,
            color = content,
            maxLines = 1,
        )
        Icon(
            imageVector = ChagokIcons.ChevronDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = content,
        )
    }
}

/** 호불호 펼침 메뉴 (화면 51). 고르면 바로 바뀐다. */
@Composable
private fun RatingMenu(good: Boolean, onPick: (good: Boolean) -> Unit) {
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(290.dp)
            .chagokShadow(ChagokTheme.elevation.level2, shape)
            .background(ChagokTheme.colors.card, shape)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RatingMenuItem(
            text = stringResource(R.string.feature_memory_good),
            icon = ChagokIcons.ThumbUp,
            selected = good,
            onClick = { onPick(true) },
        )
        RatingMenuItem(
            text = stringResource(R.string.feature_memory_bad),
            icon = ChagokIcons.ThumbDown,
            selected = !good,
            onClick = { onPick(false) },
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(ChagokTheme.colors.fill),
        )
        Text(
            text = stringResource(R.string.feature_diary_rating_note),
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

@Composable
private fun RatingMenuItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val content = if (selected) ChagokTheme.colors.onMintSoft else ChagokTheme.colors.ink

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(if (selected) ChagokTheme.colors.mintSoft else Color.Transparent)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = content)
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = ChagokTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold),
            color = content,
        )
        if (selected) {
            Icon(
                imageVector = ChagokIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = content,
            )
        }
    }
}

/**
 * 카드 안의 작은 동작. 예: [↻ 다시 말하기]
 *
 * @param soft 연한 민트 바탕을 깔면 true
 */
@Composable
private fun CardLink(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    soft: Boolean = false,
) {
    val pill = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(pill)
            .background(if (soft) ChagokTheme.colors.mintSoft else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (soft) 14.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = ChagokTheme.colors.onMintSoft,
        )
        Text(
            text = text,
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.onMintSoft,
            maxLines = 1,
        )
    }
}

/** 맨 아래 큰 [이 코스로 다시 출발하기]. */
@Composable
private fun RestartButton(
    recording: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val pill = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 68.dp else 80.dp)
            .chagokShadow(ChagokTheme.elevation.level2, pill)
            .clip(pill)
            .background(ChagokTheme.colors.mint)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ChagokIcons.Navigation,
            contentDescription = null,
            modifier = Modifier.size(if (compact) 24.dp else 28.dp),
            tint = ChagokTheme.colors.onMint,
        )
        Column {
            Text(
                text = stringResource(R.string.feature_diary_restart),
                style = ChagokTheme.typography.cardTitle.copy(
                    fontSize = if (compact) 20.sp else 24.sp,
                    lineHeight = if (compact) 26.sp else 30.sp,
                ),
                color = ChagokTheme.colors.onMint,
            )
            Text(
                text = stringResource(
                    if (recording) R.string.feature_diary_restart_continue else R.string.feature_diary_restart_today,
                ),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.onMint,
            )
        }
    }
}

/** "2026.11.09 연인과 함께한 일기" */
@Composable
internal fun diaryTitle(dateText: String, relation: RelationUi): String =
    if (relation == RelationUi.SOLO) {
        stringResource(R.string.feature_diary_detail_title_solo, dateText)
    } else {
        stringResource(R.string.feature_diary_detail_title, dateText, stringResource(relation.withLabelRes))
    }

/** "③ 수성못 산책로 · 11월 9일" */
@Composable
internal fun visitLabel(number: Int, placeName: String, monthDayText: String): String =
    stringResource(R.string.feature_diary_visit_label, circledNumber(number), placeName, monthDayText)

/** 1 → "①". 20을 넘으면 숫자 그대로 쓴다. */
private fun circledNumber(number: Int): String =
    if (number in 1..CIRCLED_COUNT) (CIRCLED_ONE + (number - 1)).toString() else number.toString()

private val RATING_BUTTON_HEIGHT = 40.dp

// 시안의 열 너비: 동선 그림 400, 나머지가 장소 카드
private const val MAP_WEIGHT = 400f
private const val LIST_WEIGHT = 796f

private const val CIRCLED_ONE = '①'
private const val CIRCLED_COUNT = 20

@Preview(name = "48 일기 상세 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DiaryDetailPreview() = ChagokTheme {
    DiaryDetailContent(
        uiState = DiaryDetailUiState(
            loaded = true,
            relation = RelationUi.COUPLE,
            dateText = "2026.11.09",
            monthDayText = "11월 9일",
            visits = listOf(
                DetailVisitUi("1", 1, "교동 뭉티기", true, "육회비빔밥이 진짜 최고였어"),
                DetailVisitUi("2", 2, "앞산 카페", false, "여기 치즈케이크가 진짜 꾸덕하고 맛있었어"),
                DetailVisitUi("3", 3, "수성못 산책로", true, null),
            ),
        ),
        actions = DetailActions(),
    )
}
