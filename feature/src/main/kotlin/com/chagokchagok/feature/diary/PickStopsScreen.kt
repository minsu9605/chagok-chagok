package com.chagokchagok.feature.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi

/**
 * 목적지·경유지 고르기 (화면 54). 다시 출발하려는 일기에 장소가 4곳 이상일 때 거친다.
 * 누른 순서대로 경유 1 → 경유 2 → 도착이 된다.
 */
@Composable
internal fun PickStopsContent(
    uiState: DiaryDetailUiState,
    picker: StopPickerUi,
    onBackClick: () -> Unit,
    onStopClick: (visitId: String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relation = uiState.relation ?: return
    val picked = picker.pickedVisitIds.mapNotNull { id -> uiState.visits.firstOrNull { it.visitId == id } }

    DiaryPage(modifier = modifier) { compact ->
        DiaryHeader(
            backText = stringResource(R.string.feature_diary_pick_back),
            title = stringResource(
                R.string.feature_diary_pick_title,
                diaryTitle(dateText = uiState.dateText, relation = relation),
            ),
            compact = compact,
            onBackClick = onBackClick,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(STOPS_WEIGHT)
                    .fillMaxHeight()
                    .background(ChagokTheme.colors.card, ChagokTheme.shapes.widget)
                    .padding(horizontal = if (compact) 20.dp else 24.dp, vertical = if (compact) 18.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.feature_diary_pick_question),
                    style = ChagokTheme.typography.cardTitle.copy(fontSize = if (compact) 22.sp else 26.sp),
                    color = ChagokTheme.colors.ink,
                )
                Text(
                    text = stringResource(R.string.feature_diary_pick_description),
                    style = ChagokTheme.typography.caption.copy(fontSize = 14.sp, lineHeight = 20.sp),
                    color = ChagokTheme.colors.inkSecondary,
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = uiState.visits, key = { it.visitId }) { visit ->
                        val order = picker.pickedVisitIds.indexOf(visit.visitId)
                        StopRow(
                            placeName = visit.placeName,
                            order = order,
                            last = order == picker.pickedVisitIds.lastIndex,
                            compact = compact,
                            onClick = { onStopClick(visit.visitId) },
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(PLAN_WEIGHT)
                    .fillMaxHeight()
                    .background(ChagokTheme.colors.card, ChagokTheme.shapes.widget)
                    .padding(horizontal = if (compact) 18.dp else 24.dp, vertical = if (compact) 18.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.feature_diary_pick_plan_title),
                    style = ChagokTheme.typography.cardTitle.copy(fontSize = if (compact) 19.sp else 22.sp),
                    color = ChagokTheme.colors.ink,
                )
                Column(modifier = Modifier.weight(1f)) {
                    PlanStep(text = stringResource(R.string.feature_diary_pick_here)) {
                        Box(
                            modifier = Modifier
                                .size(PLAN_NODE_SIZE)
                                .background(ChagokTheme.colors.mintSoft, ChagokTheme.shapes.pill),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(ChagokTheme.colors.mintInk, ChagokTheme.shapes.pill),
                            )
                        }
                    }
                    if (picked.isEmpty()) {
                        PlanLine()
                        Text(
                            text = stringResource(R.string.feature_diary_pick_wait),
                            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp),
                            color = ChagokTheme.colors.inkSecondary,
                        )
                    }
                    picked.forEachIndexed { index, visit ->
                        val end = index == picked.lastIndex
                        PlanLine()
                        PlanStep(text = visit.placeName) {
                            Box(
                                modifier = Modifier
                                    .size(PLAN_NODE_SIZE)
                                    .background(
                                        if (end) ChagokTheme.colors.ink else ChagokTheme.colors.mint,
                                        ChagokTheme.shapes.pill,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (end) {
                                    Icon(
                                        imageVector = ChagokIcons.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White,
                                    )
                                } else {
                                    Text(
                                        text = (index + 1).toString(),
                                        style = ChagokTheme.typography.label,
                                        color = ChagokTheme.colors.onMint,
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.feature_diary_pick_note, uiState.monthDayText),
                    style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = ChagokTheme.colors.inkSecondary,
                )
                ChagokButton(
                    text = stringResource(R.string.feature_diary_pick_go),
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    height = if (compact) 60.dp else 76.dp,
                    icon = ChagokIcons.Navigation,
                    elevated = true,
                    enabled = picked.isNotEmpty(),
                )
            }
        }
    }
}

/**
 * 고를 장소 한 줄.
 *
 * @param order 고른 순서(0부터). 고르지 않았으면 -1
 * @param last 고른 것 중 마지막(도착지)이면 true
 */
@Composable
private fun StopRow(
    placeName: String,
    order: Int,
    last: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    val on = order >= 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 54.dp else 66.dp)
            .clip(shape)
            .background(if (on) ChagokTheme.colors.mintSoft else ChagokTheme.colors.background)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            !on -> Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(2.dp, ChagokTheme.colors.fill, ChagokTheme.shapes.pill),
            )
            else -> Box(
                modifier = Modifier
                    .height(32.dp)
                    .background(
                        if (last) ChagokTheme.colors.ink else ChagokTheme.colors.mint,
                        ChagokTheme.shapes.pill,
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (last) {
                        stringResource(R.string.feature_diary_pick_arrive)
                    } else {
                        stringResource(R.string.feature_diary_pick_via, order + 1)
                    },
                    style = ChagokTheme.typography.label,
                    color = if (last) Color.White else ChagokTheme.colors.onMint,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = placeName,
            modifier = Modifier.weight(1f),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = if (compact) 18.sp else 21.sp),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlanStep(text: String, node: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        node()
        Text(
            text = text,
            style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 순서 사이를 잇는 짧은 세로선. */
@Composable
private fun PlanLine() {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(PLAN_NODE_SIZE)
            .height(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(14.dp)
                .background(ChagokTheme.colors.fill),
        )
    }
}

private val PLAN_NODE_SIZE = 28.dp

// 시안의 열 너비: 장소 목록 776, 출발 순서 420
private const val STOPS_WEIGHT = 776f
private const val PLAN_WEIGHT = 420f

@Preview(name = "54 경유지 고르기 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun PickStopsPreview() = ChagokTheme {
    PickStopsContent(
        uiState = DiaryDetailUiState(
            loaded = true,
            relation = RelationUi.FRIEND,
            dateText = "2026.08.15",
            monthDayText = "8월 15일",
            visits = listOf(
                DetailVisitUi("1", 1, "황리단길", true, null),
                DetailVisitUi("2", 2, "대릉원 앞 한옥 카페", true, null),
                DetailVisitUi("3", 3, "첨성대", true, null),
                DetailVisitUi("4", 4, "동궁과 월지", true, null),
                DetailVisitUi("5", 5, "보문호수", true, null),
            ),
        ),
        picker = StopPickerUi(pickedVisitIds = listOf("1", "2", "4")),
        onBackClick = {},
        onStopClick = {},
        onConfirmClick = {},
    )
}
