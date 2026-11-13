package com.chagokchagok.feature.record.relation

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokDialog
import com.chagokchagok.core.designsystem.component.pressScale
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.record.home.RelationPickerMode
import com.chagokchagok.feature.record.home.RelationPickerUi

/**
 * 갈 곳이 정해진 채로 관계를 물을 때 덧붙이는 안내 (화면 19-1, 55).
 *
 * @property chipText 맨 위 칩. 예: "지난 일기에서 다시 출발"
 * @property noteText 아래 안내. 예: "새 기록은 오늘 날짜로 쌓이고, 지난 일기는 그대로예요."
 */
internal data class RelationPickerGuide(
    val chipText: String,
    val noteText: String,
)

/**
 * 관계 고르기 팝업 (화면 05 2인, 06 3인 이상, 07 다른 모드 직접 선택).
 * 화면 내용 위에 겹쳐 놓는다. 음성으로 답하는 안내는 2차라 넣지 않았다.
 */
@Composable
internal fun RelationPickerDialog(
    picker: RelationPickerUi,
    onRelationClick: (RelationUi) -> Unit,
    onShowAllClick: () -> Unit,
    onBackToRecommendedClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    guide: RelationPickerGuide? = null,
) {
    val showAll = picker.mode == RelationPickerMode.ALL

    ChagokDialog(
        modifier = modifier,
        width = if (showAll) 980.dp else 720.dp,
        onClose = onCloseClick,
        closeContentDescription = stringResource(R.string.feature_relation_picker_close),
    ) {
        if (showAll) {
            AllRelationsContent(
                onRelationClick = onRelationClick,
                onBackToRecommendedClick = onBackToRecommendedClick,
            )
        } else {
            RecommendedRelationsContent(
                picker = picker,
                guide = guide,
                onRelationClick = onRelationClick,
                onShowAllClick = onShowAllClick,
            )
        }
    }
}

/** 화면 05 · 06: 좌석 인원에 맞춘 관계 2개. */
@Composable
private fun ColumnScope.RecommendedRelationsContent(
    picker: RelationPickerUi,
    guide: RelationPickerGuide?,
    onRelationClick: (RelationUi) -> Unit,
    onShowAllClick: () -> Unit,
) {
    val isTwo = picker.mode == RelationPickerMode.TWO

    if (guide != null) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChagokChip(
                text = guide.chipText,
                colors = ChagokChipDefaults.mint(),
                icon = ChagokIcons.Navigation,
                height = 40.dp,
            )
            ChagokChip(
                text = stringResource(R.string.feature_relation_picker_seat_detected, picker.seatCount),
                colors = ChagokChipDefaults.background(),
                height = 40.dp,
            )
        }
    } else {
        ChagokChip(
            text = stringResource(R.string.feature_relation_picker_seat_detected, picker.seatCount),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ChagokChipDefaults.mint(),
            icon = ChagokIcons.Users,
            height = 44.dp,
        )
    }
    Text(
        text = stringResource(
            if (isTwo) R.string.feature_relation_picker_title_two else R.string.feature_relation_picker_title_group,
        ),
        modifier = Modifier.fillMaxWidth(),
        style = ChagokTheme.typography.screenTitle,
        color = ChagokTheme.colors.ink,
        textAlign = TextAlign.Center,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isTwo) {
            RelationTile(
                relation = RelationUi.COUPLE,
                label = stringResource(RelationUi.COUPLE.shortLabelRes),
                height = RECOMMENDED_TILE_HEIGHT,
                onClick = { onRelationClick(RelationUi.COUPLE) },
                modifier = Modifier.weight(1f),
            )
            RelationTile(
                relation = RelationUi.FRIEND,
                label = stringResource(RelationUi.FRIEND.shortLabelRes),
                height = RECOMMENDED_TILE_HEIGHT,
                onClick = { onRelationClick(RelationUi.FRIEND) },
                modifier = Modifier.weight(1f),
            )
        } else {
            RelationTile(
                relation = RelationUi.FAMILY,
                label = stringResource(RelationUi.FAMILY.shortLabelRes),
                height = RECOMMENDED_TILE_HEIGHT,
                onClick = { onRelationClick(RelationUi.FAMILY) },
                modifier = Modifier.weight(1f),
            )
            RelationTile(
                relation = RelationUi.FRIEND,
                label = stringResource(R.string.feature_relation_picker_tile_friends),
                height = RECOMMENDED_TILE_HEIGHT,
                onClick = { onRelationClick(RelationUi.FRIEND) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    if (guide != null) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ChagokIcons.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = guide.noteText,
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
    } else {
        TextLink(
            text = stringResource(R.string.feature_relation_picker_show_all),
            onClick = onShowAllClick,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/** 화면 07: 관계 4개 모두. 창이 좁으면 2줄로 놓는다. */
@Composable
private fun ColumnScope.AllRelationsContent(
    onRelationClick: (RelationUi) -> Unit,
    onBackToRecommendedClick: () -> Unit,
) {
    BackChip(
        text = stringResource(R.string.feature_relation_picker_back),
        onClick = onBackToRecommendedClick,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.feature_relation_picker_title_all),
            style = ChagokTheme.typography.screenTitle,
            color = ChagokTheme.colors.ink,
        )
        Text(
            text = stringResource(R.string.feature_relation_picker_description_all),
            style = ChagokTheme.typography.body.copy(fontSize = 17.sp, lineHeight = 25.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val relations = listOf(RelationUi.SOLO, RelationUi.COUPLE, RelationUi.FRIEND, RelationUi.FAMILY)
        val twoRows = maxWidth < ALL_TILES_ONE_ROW_MIN_WIDTH
        val rows = if (twoRows) relations.chunked(2) else listOf(relations)

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            rows.forEach { rowRelations ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    rowRelations.forEach { relation ->
                        RelationTile(
                            relation = relation,
                            label = stringResource(relation.shortLabelRes),
                            height = if (twoRows) ALL_TILE_HEIGHT_TWO_ROWS else ALL_TILE_HEIGHT,
                            onClick = { onRelationClick(relation) },
                            modifier = Modifier.weight(1f),
                            small = true,
                        )
                    }
                }
            }
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ChagokTheme.colors.mintSoft, ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ChagokIcons.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChagokTheme.colors.mintInk,
            )
        }
        Text(
            text = stringResource(R.string.feature_relation_picker_footer_all),
            style = ChagokTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
    }
}

/**
 * 관계 타일: 관계의 연한 바탕 + 흰 동그라미 안 아이콘 + 이름.
 *
 * @param small 4개를 나란히 놓을 때 true. 아이콘과 글자를 조금 줄인다.
 */
@Composable
private fun RelationTile(
    relation: RelationUi,
    label: String,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val palette = relation.palette
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .pressScale(interactionSource)
            .height(height)
            .clip(TileShape)
            .background(palette.soft)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(if (small) 12.dp else 14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(if (small) 60.dp else 68.dp)
                .chagokShadow(ChagokTheme.elevation.level1, ChagokTheme.shapes.pill)
                .background(ChagokTheme.colors.card, ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = relation.icon,
                contentDescription = null,
                modifier = Modifier.size(if (small) 28.dp else 30.dp),
                tint = palette.ink,
            )
        }
        Text(
            text = label,
            style = ChagokTheme.typography.cardTitle.copy(fontSize = if (small) 24.sp else 26.sp),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

/** "다른 모드 직접 선택 ›" */
@Composable
private fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(ChagokTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = ChagokTheme.typography.label.copy(fontSize = 17.sp, lineHeight = 24.sp),
            color = ChagokTheme.colors.mintInk,
        )
        Icon(
            imageVector = ChagokIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ChagokTheme.colors.mintInk,
        )
    }
}

/** "‹ 추천으로 돌아가기" */
@Composable
private fun BackChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(ChagokTheme.shapes.pill)
            .background(ChagokTheme.colors.background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 14.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ChagokIcons.ChevronLeft,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ChagokTheme.colors.ink,
        )
        Text(
            text = text,
            style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
            color = ChagokTheme.colors.ink,
        )
    }
}

private val TileShape = RoundedCornerShape(30.dp)
private val RECOMMENDED_TILE_HEIGHT = 176.dp
private val ALL_TILE_HEIGHT = 200.dp
private val ALL_TILE_HEIGHT_TWO_ROWS = 132.dp

/** 관계 4개를 한 줄에 놓을 수 있는 최소 너비. 이보다 좁으면 2줄로 놓는다. */
private val ALL_TILES_ONE_ROW_MIN_WIDTH = 560.dp

@Composable
private fun RelationPickerPreview(mode: RelationPickerMode, seatCount: Int) {
    ChagokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChagokTheme.colors.background),
        ) {
            RelationPickerDialog(
                picker = RelationPickerUi(mode = mode, seatCount = seatCount),
                onRelationClick = {},
                onShowAllClick = {},
                onBackToRecommendedClick = {},
                onCloseClick = {},
            )
        }
    }
}

@Preview(name = "05 관계 · 2인 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun RelationTwoPreview() = RelationPickerPreview(RelationPickerMode.TWO, seatCount = 2)

@Preview(name = "06 관계 · 3인 이상 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun RelationGroupPreview() = RelationPickerPreview(RelationPickerMode.THREE_OR_MORE, seatCount = 3)

@Preview(name = "07 관계 · 직접 선택 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun RelationAllPreview() = RelationPickerPreview(RelationPickerMode.ALL, seatCount = 2)

@Preview(name = "07 관계 · 직접 선택 · 전체 화면", widthDp = 1280, heightDp = 690)
@Composable
private fun RelationAllWidePreview() = RelationPickerPreview(RelationPickerMode.ALL, seatCount = 2)

@Preview(name = "07 관계 · 직접 선택 · 작은 창", widthDp = 426, heightDp = 605)
@Composable
private fun RelationAllNarrowPreview() = RelationPickerPreview(RelationPickerMode.ALL, seatCount = 2)
