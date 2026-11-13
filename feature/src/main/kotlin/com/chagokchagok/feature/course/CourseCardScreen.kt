package com.chagokchagok.feature.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokDialog
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.directionParticle
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.diary.DiaryHeader
import com.chagokchagok.feature.diary.DiaryPage
import com.chagokchagok.feature.diary.tabLabelRes
import com.chagokchagok.feature.record.complete.RouteMap

/**
 * 코스 완성 카드 (화면 42, 43)와 장소 바꾸기 팝업 (화면 42-1).
 * 왼쪽은 코스 동선 그림, 오른쪽은 가는 순서대로 놓은 장소 카드다.
 */
@Composable
internal fun CourseCardContent(
    uiState: CourseUiState,
    actions: CourseActions,
    modifier: Modifier = Modifier,
) {
    val relation = uiState.relation

    Box(modifier = modifier.fillMaxSize()) {
        DiaryPage { compact ->
            DiaryHeader(
                backText = stringResource(R.string.feature_course_restart),
                title = stringResource(R.string.feature_course_card_title, stringResource(relation.tabLabelRes)),
                compact = compact,
                onBackClick = actions.onRestartClick,
            ) {
                ChagokChip(
                    text = stringResource(relation.tabLabelRes),
                    colors = ChagokChipDefaults.relation(relation.palette),
                    icon = relation.icon,
                    height = 34.dp,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
            ) {
                // 실제 지도와 좌표가 아니다. 가는 순서만 보여 주는 그림이다.
                RouteMap(
                    relation = relation,
                    placeNames = uiState.items.map { it.name },
                    modifier = Modifier
                        .weight(MAP_WEIGHT)
                        .fillMaxHeight(),
                    labelRes = R.string.feature_course_route,
                    originLabelRes = R.string.feature_course_here,
                )
                Column(
                    modifier = Modifier
                        .weight(LIST_WEIGHT)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val swappedNote = uiState.swappedNote
                    if (swappedNote != null) {
                        val (index, name) = swappedNote
                        ChagokChip(
                            text = stringResource(
                                R.string.feature_course_swapped,
                                index + 1,
                                name,
                                directionParticle(name),
                            ),
                            colors = ChagokChipDefaults.mint(),
                            icon = ChagokIcons.Check,
                            height = 36.dp,
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(items = uiState.items) { index, item ->
                            CourseItemCard(
                                index = index,
                                item = item,
                                relation = relation,
                                onSwapClick = { actions.onSwapClick(index) },
                            )
                        }
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = ChagokIcons.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ChagokTheme.colors.mintInk,
                                )
                                Text(
                                    text = stringResource(R.string.feature_course_card_hint, uiState.items.size),
                                    style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                                    color = ChagokTheme.colors.inkSecondary,
                                )
                            }
                        }
                    }
                    DepartButton(recording = uiState.recording, compact = compact, onClick = actions.onDepartClick)
                }
            }
        }

        val swap = uiState.swap
        if (swap != null) {
            SwapDialog(
                swap = swap,
                current = uiState.items.getOrNull(swap.index),
                onPick = actions.onSwapPick,
                onClose = actions.onSwapClose,
            )
        }
    }
}

/** 장소 카드: "1차 · 식사", 내 기억·새 장소 칩, 이름, 덧붙이는 말, [변경]. */
@Composable
private fun CourseItemCard(
    index: Int,
    item: CourseItemUi,
    relation: RelationUi,
    onSwapClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, CardShape)
            .padding(start = 18.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderChip(index = index, category = item.category)
                if (item.fromMemory) {
                    ChagokChip(
                        text = stringResource(R.string.feature_course_my_memory),
                        colors = ChagokChipDefaults.relation(relation.palette),
                        icon = relation.icon,
                    )
                } else {
                    ChagokChip(
                        text = stringResource(R.string.feature_course_new_place),
                        colors = ChagokChipDefaults.fill(),
                    )
                }
            }
            Text(
                text = item.name,
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 28.sp),
                color = ChagokTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subText(),
                style = ChagokTheme.typography.label.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        ChagokButton(
            text = stringResource(R.string.feature_course_swap),
            onClick = onSwapClick,
            style = ChagokButtonStyle.Secondary,
            height = 48.dp,
            icon = ChagokIcons.Refresh,
        )
    }
}

/** "1차 · 식사". 첫 번째는 민트로 채우고, 나머지는 민트 테두리만 두른다. */
@Composable
private fun OrderChip(index: Int, category: String?) {
    val pill = ChagokTheme.shapes.pill
    val first = index == 0

    Box(
        modifier = Modifier
            .height(30.dp)
            .then(
                if (first) {
                    Modifier.background(ChagokTheme.colors.mint, pill)
                } else {
                    Modifier.border(2.dp, ChagokTheme.colors.mint, pill)
                },
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (category == null) {
                stringResource(R.string.feature_course_order, index + 1)
            } else {
                stringResource(R.string.feature_course_order_category, index + 1, category)
            },
            style = ChagokTheme.typography.label,
            color = ChagokTheme.colors.onMint,
            maxLines = 1,
        )
    }
}

/** "2026.10.12 방문" · "신규 추천 · 카페" */
@Composable
private fun CourseItemUi.subText(): String =
    if (fromMemory) {
        stringResource(R.string.feature_course_visited, dateText.orEmpty())
    } else {
        stringResource(R.string.feature_course_new_sub, listOfNotNull(category, distanceText).joinToString(" · "))
    }

/** 맨 아래 큰 [이 코스로 출발하기]. */
@Composable
private fun DepartButton(
    recording: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val pill = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 68.dp else 88.dp)
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
            modifier = Modifier.size(if (compact) 24.dp else 30.dp),
            tint = ChagokTheme.colors.onMint,
        )
        Column {
            Text(
                text = stringResource(R.string.feature_course_go),
                style = ChagokTheme.typography.cardTitle.copy(
                    fontSize = if (compact) 20.sp else 24.sp,
                    lineHeight = if (compact) 26.sp else 30.sp,
                ),
                color = ChagokTheme.colors.onMint,
            )
            Text(
                text = stringResource(
                    if (recording) R.string.feature_diary_restart_continue else R.string.feature_course_go_sub,
                ),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.onMint,
            )
        }
    }
}

/** 장소 바꾸기 팝업 (화면 42-1). 후보를 누르면 묻지 않고 바로 바뀐다. */
@Composable
private fun SwapDialog(
    swap: CourseSwapUi,
    current: CourseItemUi?,
    onPick: (CourseItemUi) -> Unit,
    onClose: () -> Unit,
) {
    ChagokDialog(width = 720.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OrderChip(index = swap.index, category = current?.category)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.feature_course_swap_title, swap.index + 1),
                style = ChagokTheme.typography.screenTitle.copy(fontSize = 26.sp, lineHeight = 34.sp),
                color = ChagokTheme.colors.ink,
            )
            Text(
                text = stringResource(R.string.feature_course_swap_description),
                style = ChagokTheme.typography.caption,
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        if (current != null) {
            SwapRow(item = current, current = true, onClick = onClose)
        }
        swap.candidates.forEach { candidate ->
            SwapRow(item = candidate, current = false, onClick = { onPick(candidate) })
        }
        val firstCandidate = swap.candidates.firstOrNull()
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (firstCandidate == null) ChagokIcons.Info else ChagokIcons.Mic,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = if (firstCandidate == null) {
                    stringResource(R.string.feature_course_swap_empty)
                } else {
                    stringResource(
                        R.string.feature_course_swap_hint,
                        firstCandidate.name,
                        directionParticle(firstCandidate.name),
                    )
                },
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        ChagokButton(
            text = stringResource(R.string.feature_course_swap_keep),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            style = ChagokButtonStyle.Secondary,
            height = 60.dp,
        )
    }
}

/** 팝업의 한 줄: 지금 코스(체크) 또는 후보([이 곳으로]). */
@Composable
private fun SwapRow(
    item: CourseItemUi,
    current: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (current) ChagokTheme.colors.mintSoft else ChagokTheme.colors.background)
            .then(if (current) Modifier.border(2.dp, ChagokTheme.colors.mint, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (current) ChagokTheme.colors.mint else ChagokTheme.colors.card,
                    ChagokTheme.shapes.pill,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (current) ChagokIcons.Check else ChagokIcons.Pin,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (current) ChagokTheme.colors.onMint else ChagokTheme.colors.mintInk,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 26.sp),
                color = ChagokTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subText(),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        ChagokChip(
            text = stringResource(
                if (current) R.string.feature_course_swap_current else R.string.feature_course_swap_pick,
            ),
            colors = if (current) ChagokChipDefaults.white() else ChagokChipDefaults.mint(),
            icon = if (current) null else ChagokIcons.Refresh,
            height = 40.dp,
        )
    }
}

private val CardShape = RoundedCornerShape(26.dp)

// 시안의 열 너비: 동선 그림 500, 나머지가 장소 카드
private const val MAP_WEIGHT = 500f
private const val LIST_WEIGHT = 696f

@Preview(name = "42 코스 카드 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun CourseCardPreview() = ChagokTheme {
    CourseCardContent(
        uiState = CourseUiState(
            relation = RelationUi.COUPLE,
            step = CourseStep.Card,
            items = listOf(
                CourseItemUi(name = "시골길 김치찌개", fromMemory = true, dateText = "2025.11.22"),
                CourseItemUi(name = "팔공 브리즈", fromMemory = false, category = "카페", distanceText = "약 2km"),
            ),
        ),
        actions = CourseActions(),
    )
}
