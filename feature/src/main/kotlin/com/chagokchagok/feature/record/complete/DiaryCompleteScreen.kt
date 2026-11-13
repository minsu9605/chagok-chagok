package com.chagokchagok.feature.record.complete

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.chagokchagok.feature.record.home.MapBackdrop
import com.chagokchagok.feature.record.memory.MemoDialog

/**
 * 일기 완성 화면 (화면 29). [기록 종료]로 하루 기록을 끝낸 직후에 뜬다.
 * 왼쪽은 하루 동선 그림, 오른쪽은 다녀온 곳의 카드다.
 */
@Composable
fun DiaryCompleteScreen(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryCompleteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        DiaryCompleteContent(
            uiState = uiState,
            onCloseClick = onCloseClick,
            onAddMemoClick = viewModel::onAddMemoClick,
        )
        val memoEditing = uiState.memoEditing
        if (memoEditing != null) {
            MemoDialog(
                memo = memoEditing,
                onTextChange = viewModel::onMemoTextChange,
                onSaveClick = viewModel::onMemoSaveClick,
                onLaterClick = viewModel::onMemoLaterClick,
            )
        }
    }
}

@Composable
private fun DiaryCompleteContent(
    uiState: DiaryCompleteUiState,
    onCloseClick: () -> Unit,
    onAddMemoClick: (visitId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.background),
    ) {
        val compact = maxWidth < COMPACT_MAX_WIDTH
        val relation = uiState.relation

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (compact) 24.dp else 32.dp,
                    vertical = if (compact) 16.dp else 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
        ) {
            CompleteHeader(
                uiState = uiState,
                compact = compact,
                onCloseClick = onCloseClick,
            )
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
                    )
                    VisitList(
                        relation = relation,
                        visits = uiState.visits,
                        onAddMemoClick = onAddMemoClick,
                        modifier = Modifier
                            .weight(LIST_WEIGHT)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

/** 책 아이콘 + 제목 + "2026.11.09 · 연인 모드 · 총 3곳 방문" + [확인 및 닫기] */
@Composable
private fun CompleteHeader(
    uiState: DiaryCompleteUiState,
    compact: Boolean,
    onCloseClick: () -> Unit,
) {
    val relation = uiState.relation
    // 직접 끝낸 오늘 일기는 "오늘", 뒤늦게 마감된 지난 일기는 그 날짜로 시작한다.
    val lead = uiState.pastDateText ?: stringResource(R.string.feature_complete_today)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 52.dp else 60.dp)
                .background(ChagokTheme.colors.mint, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ChagokIcons.Book,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 26.dp else 30.dp),
                tint = ChagokTheme.colors.onMint,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = when (relation) {
                    null -> stringResource(R.string.feature_complete_title_missing)
                    RelationUi.SOLO -> stringResource(R.string.feature_complete_title_solo, lead)
                    else -> stringResource(R.string.feature_complete_title, lead, stringResource(relation.withLabelRes))
                },
                style = ChagokTheme.typography.screenTitle.copy(
                    fontSize = if (compact) 21.sp else 28.sp,
                    lineHeight = if (compact) 28.sp else 36.sp,
                ),
                color = ChagokTheme.colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (relation != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetaText(text = uiState.dateText)
                    ChagokChip(
                        text = stringResource(relation.modeLabelRes),
                        colors = ChagokChipDefaults.relation(relation.palette),
                        icon = relation.icon,
                    )
                    MetaText(text = stringResource(R.string.feature_complete_visit_count, uiState.visits.size))
                }
            }
        }
        ChagokButton(
            text = stringResource(R.string.feature_complete_close),
            onClick = onCloseClick,
            height = if (compact) 52.dp else 60.dp,
            icon = ChagokIcons.Check,
            elevated = true,
        )
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
        color = ChagokTheme.colors.inkSecondary,
    )
}

/**
 * 하루 동선 그림: 출발지에서 다녀온 순서대로 선을 잇는다.
 * 실제 지도와 실제 좌표가 아니다. 지도와 좌표를 붙이기 전까지 순서만 보여 주는 그림이다.
 */
@Composable
private fun RouteMap(
    relation: RelationUi,
    placeNames: List<String>,
    modifier: Modifier = Modifier,
) {
    val lineColor = relation.palette.band
    val stops = placeNames.take(ROUTE_POSITIONS.size)

    BoxWithConstraints(modifier = modifier.clip(ChagokTheme.shapes.alert)) {
        val width = maxWidth
        val height = maxHeight

        MapBackdrop(showPins = false, modifier = Modifier.fillMaxSize())
        Canvas(modifier = Modifier.fillMaxSize()) {
            val points = listOf(HOME_POSITION) + ROUTE_POSITIONS.take(stops.size)
            points.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = lineColor,
                    start = Offset(size.width * from.first, size.height * from.second),
                    end = Offset(size.width * to.first, size.height * to.second),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        RouteNode(
            x = width * HOME_POSITION.first,
            y = height * HOME_POSITION.second,
            color = ChagokTheme.colors.ink,
            label = stringResource(R.string.feature_complete_origin),
        ) {
            Icon(
                imageVector = ChagokIcons.House,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White,
            )
        }
        stops.forEachIndexed { index, name ->
            val position = ROUTE_POSITIONS[index]
            RouteNode(
                x = width * position.first,
                y = height * position.second,
                color = lineColor,
                label = name,
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
            }
        }
        Text(
            text = stringResource(
                R.string.feature_complete_route,
                stops.indices.joinToString(separator = "") { " → ${CIRCLED_NUMBERS[it]}" },
            ),
            modifier = Modifier
                .padding(14.dp)
                .chagokShadow(ChagokTheme.elevation.level1, ChagokTheme.shapes.pill)
                .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 동선 위의 동그라미 하나와 그 아래 이름표. ([x], [y])가 동그라미의 가운데다. */
@Composable
private fun RouteNode(
    x: Dp,
    y: Dp,
    color: Color,
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.offset(x = x - NODE_SIZE / 2, y = y - NODE_SIZE / 2),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(NODE_SIZE)
                .background(color, ChagokTheme.shapes.pill)
                .border(3.dp, Color.White, ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        Text(
            text = label,
            style = ChagokTheme.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun VisitList(
    relation: RelationUi,
    visits: List<CompleteVisitUi>,
    onAddMemoClick: (visitId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (visits.isEmpty()) {
        Box(
            modifier = modifier.background(ChagokTheme.colors.card, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.feature_complete_empty),
                style = ChagokTheme.typography.body,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        return
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items = visits, key = { _, visit -> visit.visitId }) { index, visit ->
            VisitCard(
                number = index + 1,
                relation = relation,
                visit = visit,
                onAddMemoClick = { onAddMemoClick(visit.visitId) },
            )
        }
    }
}

/** 장소 카드: 순서, 이름, 평가, 도착 시각과 머문 시간, 한마디. */
@Composable
private fun VisitCard(
    number: Int,
    relation: RelationUi,
    visit: CompleteVisitUi,
    onAddMemoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, RoundedCornerShape(26.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(relation.palette.band, ChagokTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString(),
                    style = ChagokTheme.typography.caption.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
            }
            Text(
                text = visit.placeName,
                modifier = Modifier.weight(1f, fill = false),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 28.sp),
                color = ChagokTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ChagokChip(
                text = stringResource(if (visit.good) R.string.feature_memory_good else R.string.feature_memory_bad),
                colors = if (visit.good) ChagokChipDefaults.mint() else ChagokChipDefaults.fill(),
                icon = if (visit.good) ChagokIcons.ThumbUp else ChagokIcons.ThumbDown,
            )
        }
        Text(
            text = visit.timeText(),
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            color = ChagokTheme.colors.inkSecondary,
        )
        if (visit.memo != null) {
            Text(
                text = stringResource(R.string.feature_memory_saved_memo_quote, visit.memo),
                style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold, lineHeight = 23.sp),
                color = ChagokTheme.colors.ink,
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = ChagokTheme.colors.onLemon,
                )
            }
            ChagokButton(
                text = stringResource(R.string.feature_complete_add_memo),
                onClick = onAddMemoClick,
                style = ChagokButtonStyle.Soft,
                height = 48.dp,
                icon = ChagokIcons.Mic,
            )
        }
    }
}

/** "11:20 도착 · 1시간 15분" */
@Composable
private fun CompleteVisitUi.timeText(): String {
    val arrived = stringResource(R.string.feature_complete_arrived_at, arrivedAtText)
    val minutes = stayMinutes ?: return arrived
    val hours = (minutes / MINUTES_PER_HOUR).toInt()
    val rest = (minutes % MINUTES_PER_HOUR).toInt()
    val stay = when {
        hours == 0 -> stringResource(R.string.feature_complete_stay_minutes, rest)
        rest == 0 -> pluralStringResource(R.plurals.feature_complete_stay_hours, hours, hours)
        else -> stringResource(R.string.feature_complete_stay_hours_minutes, hours, rest)
    }
    return stringResource(R.string.feature_complete_time, arrived, stay)
}

private const val MINUTES_PER_HOUR = 60L
private val COMPACT_MAX_WIDTH = 1000.dp
private val NODE_SIZE = 36.dp

// 시안의 열 너비: 동선 그림 500, 나머지가 장소 카드
private const val MAP_WEIGHT = 500f
private const val LIST_WEIGHT = 696f

// 동선 그림에서 출발지와 방문지를 놓는 자리 (그림 크기에 대한 비율). 실제 좌표가 아니다.
private val HOME_POSITION = 0.18f to 0.80f
private val ROUTE_POSITIONS = listOf(
    0.34f to 0.55f,
    0.60f to 0.33f,
    0.78f to 0.60f,
    0.52f to 0.76f,
    0.30f to 0.26f,
    0.80f to 0.22f,
)
private val CIRCLED_NUMBERS = listOf("①", "②", "③", "④", "⑤", "⑥")

@Preview(name = "29 일기 완성 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DiaryCompletePreview() = ChagokTheme {
    DiaryCompleteContent(
        uiState = DiaryCompleteUiState(
            loaded = true,
            relation = RelationUi.COUPLE,
            dateText = "2026.11.09",
            visits = listOf(
                CompleteVisitUi("1", "교동 뭉티기", true, "11:20", 75, "육회비빔밥이 진짜 최고였어"),
                CompleteVisitUi("2", "앞산 카페", true, "13:05", 52, "여기 치즈케이크가 진짜 꾸덕하고 맛있었어"),
                CompleteVisitUi("3", "수성못 산책로", true, "15:10", 40, null),
            ),
        ),
        onCloseClick = {},
        onAddMemoClick = {},
    )
}
