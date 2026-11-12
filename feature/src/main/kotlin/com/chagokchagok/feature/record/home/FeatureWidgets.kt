package com.chagokchagok.feature.record.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chagokchagok.core.designsystem.component.ChagokWidgetCard
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R

/*
 * 홈의 일기 보기 · 코스 짜기 · 기억 물어보기 위젯.
 * 세 기능은 2차에서 만든다. 1차에서는 시안 모양만 그리고, 누르면 홈이 "준비 중이에요" 토스트를 띄운다.
 * 기능을 만들 때 onClick에 그 기능의 화면으로 가는 동작을 넘긴다.
 */

/** 일기 보기 위젯. 핀과 "일기 n권 · 핀 n곳"은 일기 기능을 만들 때 넣는다. */
@Composable
internal fun DiaryWidget(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ChagokWidgetCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MapBackdrop(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(ChagokTheme.shapes.innerCard),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WidgetTitle(text = stringResource(R.string.feature_home_diary_title))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ChagokTheme.colors.fill, ChagokTheme.shapes.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ChagokIcons.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = ChagokTheme.colors.ink,
                    )
                }
            }
        }
    }
}

/**
 * 코스 짜기 위젯.
 *
 * @param compact 창이 좁을 때 true. 예시 문장을 빼고 글자를 줄인다.
 */
@Composable
internal fun CourseWidget(compact: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ChagokWidgetCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ChagokTheme.colors.mintSoft, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ChagokIcons.Map,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ChagokTheme.colors.mintInk,
                    )
                }
                WidgetTitle(text = stringResource(R.string.feature_home_course_title))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.feature_home_course_description),
                style = ChagokTheme.typography.body.copy(
                    fontSize = if (compact) 15.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = if (compact) 22.sp else 25.sp,
                ),
                color = ChagokTheme.colors.ink,
            )
            if (!compact) {
                Spacer(modifier = Modifier.weight(1f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExamplePill(
                        text = stringResource(R.string.feature_home_course_chip_dessert),
                        modifier = Modifier.fillMaxWidth(),
                        icon = ChagokIcons.Coffee,
                    )
                    ExamplePill(
                        text = stringResource(R.string.feature_home_course_chip_nearby),
                        modifier = Modifier.fillMaxWidth(),
                        icon = ChagokIcons.Pin,
                    )
                }
            }
        }
    }
}

/**
 * 기억 물어보기 위젯.
 *
 * @param compact 창이 좁을 때 true. 마이크를 줄이고 예시 문장을 뺀다.
 */
@Composable
internal fun AskWidget(compact: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val circle = ChagokTheme.shapes.pill

    ChagokWidgetCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = if (compact) {
            PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        } else {
            PaddingValues(horizontal = 28.dp, vertical = 24.dp)
        },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 96.dp else 136.dp)
                    .background(ChagokTheme.colors.mintSoft, circle),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 74.dp else 104.dp)
                        .chagokShadow(ChagokTheme.elevation.level2, circle)
                        .background(ChagokTheme.colors.mint, circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ChagokIcons.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 32.dp else 44.dp),
                        tint = ChagokTheme.colors.onMint,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.feature_home_ask_title),
                    style = if (compact) {
                        ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 28.sp)
                    } else {
                        ChagokTheme.typography.cardTitle
                    },
                    color = ChagokTheme.colors.ink,
                )
                Text(
                    text = stringResource(R.string.feature_home_ask_description),
                    style = ChagokTheme.typography.body.copy(
                        fontSize = if (compact) 15.sp else 16.sp,
                        lineHeight = if (compact) 22.sp else 24.sp,
                    ),
                    color = ChagokTheme.colors.inkSecondary,
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExamplePill(text = stringResource(R.string.feature_home_ask_example_cafe))
                        ExamplePill(text = stringResource(R.string.feature_home_ask_example_visited))
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetTitle(text: String) {
    Text(
        text = text,
        style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 28.sp),
        color = ChagokTheme.colors.ink,
    )
}

/** 예시 문장 알약. 예: "우리가 좋아했던 디저트 코스" */
@Composable
private fun ExamplePill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .background(ChagokTheme.colors.background, ChagokTheme.shapes.pill)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChagokTheme.colors.mintInk,
            )
        }
        Text(
            text = text,
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 지도 바탕 그림: 땅 위에 공원·물·길만 그린다. 실제 지도가 아니라 시안의 지도 그림을 흉내 낸 것이다.
 * 색의 기준: 화면 설계 개요 2-1의 지도 색. 지도 기능은 2차라 테마에 넣지 않고 여기서만 쓴다.
 */
@Composable
private fun MapBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(MapGround)) {
        val width = size.width
        val height = size.height
        val roadWidth = 9.dp.toPx()
        val roadEdgeWidth = roadWidth + 3.dp.toPx()

        // 공원 (왼쪽 위)
        drawRoundRect(
            color = MapPark,
            topLeft = Offset(-width * 0.05f, -height * 0.1f),
            size = Size(width * 0.4f, height * 0.36f),
            cornerRadius = CornerRadius(22.dp.toPx()),
        )
        // 물: 가로로 흐르는 강과 오른쪽 아래 못
        drawLine(
            color = MapWater,
            start = Offset(0f, height * 0.72f),
            end = Offset(width, height * 0.56f),
            strokeWidth = 16.dp.toPx(),
        )
        drawOval(
            color = MapWater,
            topLeft = Offset(width * 0.68f, height * 0.78f),
            size = Size(width * 0.26f, height * 0.2f),
        )
        // 길: 테두리를 먼저 그리고 그 위에 흰 길을 그린다.
        val roads = listOf(
            Offset(0f, height * 0.36f) to Offset(width, height * 0.26f),
            Offset(width * 0.38f, 0f) to Offset(width * 0.46f, height),
            Offset(width * 0.7f, 0f) to Offset(width * 0.66f, height),
        )
        roads.forEach { (start, end) ->
            drawLine(color = MapRoadEdge, start = start, end = end, strokeWidth = roadEdgeWidth, cap = StrokeCap.Butt)
        }
        roads.forEach { (start, end) ->
            drawLine(color = MapRoad, start = start, end = end, strokeWidth = roadWidth, cap = StrokeCap.Butt)
        }
    }
}

private val MapGround = Color(0xFFF2F0EA)
private val MapPark = Color(0xFFD5ECCB)
private val MapWater = Color(0xFFB8DCF5)
private val MapRoad = Color(0xFFFFFFFF)
private val MapRoadEdge = Color(0xFFE2DED4)
