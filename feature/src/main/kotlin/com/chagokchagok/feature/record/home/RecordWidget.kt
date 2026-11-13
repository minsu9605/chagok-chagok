package com.chagokchagok.feature.record.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.chagokchagok.core.designsystem.component.ChagokButton
import com.chagokchagok.core.designsystem.component.ChagokButtonStyle
import com.chagokchagok.core.designsystem.component.ChagokChip
import com.chagokchagok.core.designsystem.component.ChagokChipDefaults
import com.chagokchagok.core.designsystem.component.ChagokWidgetCard
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette

/**
 * 쌓기 위젯 · 기록 꺼짐 (화면 02). 위젯 전체를 누르면 기록을 시작한다.
 *
 * @param compact 창이 좁을 때 true. 설명 글자를 줄인다.
 */
@Composable
internal fun RecordIdleWidget(
    todayPlaceCount: Int,
    lastRecord: LastRecordUi?,
    compact: Boolean,
    onStartRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChagokWidgetCard(modifier = modifier, onClick = onStartRecordClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecordWidgetHeader(
                meta = stringResource(R.string.feature_home_record_today_count, todayPlaceCount),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                StartRecordButton(compact = compact)
            }
            Text(
                text = stringResource(R.string.feature_home_record_description),
                style = ChagokTheme.typography.body.copy(
                    fontSize = if (compact) 15.sp else 17.sp,
                    lineHeight = if (compact) 23.sp else 26.sp,
                ),
                color = ChagokTheme.colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
            if (lastRecord != null) {
                Spacer(modifier = Modifier.height(14.dp))
                LastRecordChip(lastRecord = lastRecord)
            }
        }
    }
}

/**
 * 큰 동그란 [쌓기 시작]. 위젯이 낮으면 함께 작아진다. 시안 크기는 바깥 236, 안쪽 184.
 * 누르기를 기다리는 동안 물결 두 겹이 퍼지고 버튼이 살짝 커졌다 돌아온다 (웹 데모 recWave, recHum).
 *
 * @param compact 창이 좁을 때 true. 물결이 위젯 밖으로 나가지 않게 버튼과 물결을 줄인다.
 */
@Composable
private fun StartRecordButton(compact: Boolean) {
    val circle = ChagokTheme.shapes.pill
    val waveColor = ChagokTheme.colors.mint
    val maxSize = if (compact) 200.dp else 236.dp
    val waveEndScale = if (compact) COMPACT_WAVE_END_SCALE else WAVE_END_SCALE

    val transition = rememberInfiniteTransition(label = "startRecord")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = WAVE_MILLIS, easing = LinearEasing)),
        label = "wavePhase",
    )
    val buttonScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = HUM_MILLIS
                1f at 0
                HUM_SCALE at HUM_MILLIS * 20 / 100
                1f at HUM_MILLIS * 62 / 100
                1f at HUM_MILLIS
            },
        ),
        label = "buttonScale",
    )

    Box(
        modifier = Modifier
            .sizeIn(maxWidth = maxSize, maxHeight = maxSize)
            .fillMaxHeight()
            .aspectRatio(1f)
            .drawBehind {
                // 물결 두 겹: 반 박자 어긋나게 퍼지며 옅어진다.
                repeat(WAVE_COUNT) { index ->
                    val progress = (wavePhase + index.toFloat() / WAVE_COUNT) % 1f
                    val eased = WaveEasing.transform(progress)
                    val alpha = if (progress < WAVE_FADE_POINT) {
                        lerp(WAVE_START_ALPHA, WAVE_MID_ALPHA, progress / WAVE_FADE_POINT)
                    } else {
                        lerp(WAVE_MID_ALPHA, 0f, (progress - WAVE_FADE_POINT) / (1f - WAVE_FADE_POINT))
                    }
                    drawCircle(
                        color = waveColor,
                        radius = size.minDimension / 2f * lerp(WAVE_START_SCALE, waveEndScale, eased),
                        alpha = alpha,
                    )
                }
            }
            .background(ChagokTheme.colors.mintSoft, circle),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(START_BUTTON_RATIO)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .chagokShadow(ChagokTheme.elevation.level2, circle)
                .background(ChagokTheme.colors.mint, circle),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = ChagokIcons.Plus,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = ChagokTheme.colors.onMint,
            )
            Text(
                text = stringResource(R.string.feature_home_record_start),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 22.sp, lineHeight = 28.sp),
                color = ChagokTheme.colors.onMint,
            )
        }
    }
}

/** "지난 기록 · 10월 24일 가족과 2곳" */
@Composable
private fun LastRecordChip(lastRecord: LastRecordUi) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(ChagokTheme.colors.background, ChagokTheme.shapes.pill)
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(lastRecord.relation.palette.band, ChagokTheme.shapes.pill),
        )
        Text(
            text = stringResource(
                R.string.feature_home_record_last,
                lastRecord.dateText,
                stringResource(lastRecord.relation.withLabelRes),
                lastRecord.placeCount,
            ),
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp),
            color = ChagokTheme.colors.ink,
            maxLines = 1,
        )
    }
}

/**
 * 쌓기 위젯 · 기록 중 현황 카드 (화면 10, 구조 A).
 *
 * @param compact 창이 좁을 때 true. 버튼 글자가 잘리지 않도록 버튼을 낮추고 위아래로 놓는다.
 */
@Composable
internal fun RecordActiveWidget(
    recording: RecordingUi,
    compact: Boolean,
    onGoToNaviClick: () -> Unit,
    onEndRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relation = recording.relation
    val modeLabel = stringResource(relation.modeLabelRes)
    // '나 혼자'는 인원을 붙이지 않는다.
    val showSeatCount = relation != RelationUi.SOLO
    val buttonHeight = if (compact) 56.dp else 64.dp

    ChagokWidgetCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecordWidgetHeader(
                meta = stringResource(R.string.feature_home_recording_started_at, recording.startedAtText),
            )
            Spacer(modifier = Modifier.height(if (compact) 14.dp else 22.dp))
            ChagokChip(
                text = if (showSeatCount) {
                    stringResource(R.string.feature_home_recording_mode_chip, modeLabel, recording.seatCount)
                } else {
                    modeLabel
                },
                colors = ChagokChipDefaults.relation(relation.palette),
                icon = relation.icon,
                height = 36.dp,
            )
            Spacer(modifier = Modifier.height(if (compact) 14.dp else 20.dp))
            TodayPlaces(placeNames = recording.placeNames)
            Spacer(modifier = Modifier.weight(1f))
            val goNaviButton: @Composable (Modifier) -> Unit = { buttonModifier ->
                ChagokButton(
                    text = stringResource(R.string.feature_home_recording_go_navi),
                    onClick = onGoToNaviClick,
                    modifier = buttonModifier,
                    height = buttonHeight,
                    icon = ChagokIcons.Navigation,
                )
            }
            val endRecordButton: @Composable (Modifier) -> Unit = { buttonModifier ->
                ChagokButton(
                    text = stringResource(R.string.feature_home_recording_end),
                    onClick = onEndRecordClick,
                    modifier = buttonModifier,
                    style = ChagokButtonStyle.Secondary,
                    height = buttonHeight,
                    icon = ChagokIcons.Stop,
                )
            }
            if (compact) {
                // 좁은 위젯에서는 옆으로 놓으면 글자가 잘려 위아래로 놓는다.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goNaviButton(Modifier.fillMaxWidth())
                    endRecordButton(Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    goNaviButton(Modifier.weight(1f))
                    endRecordButton(Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.feature_home_recording_footer),
                modifier = Modifier.fillMaxWidth(),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** "오늘 다녀온 곳 · 1곳 [교동 뭉티기]". 이름은 2곳까지만 보이고 나머지는 "외 n곳". */
@Composable
private fun TodayPlaces(placeNames: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.background, ChagokTheme.shapes.innerCard)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ChagokIcons.Pin,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = stringResource(R.string.feature_home_recording_today_label),
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.feature_home_recording_place_count, placeNames.size),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 26.sp),
                color = ChagokTheme.colors.ink,
            )
            if (placeNames.isEmpty()) {
                Text(
                    text = stringResource(R.string.feature_home_recording_no_place),
                    style = ChagokTheme.typography.caption,
                    color = ChagokTheme.colors.inkSecondary,
                )
            } else {
                placeNames.take(MAX_VISIBLE_PLACE_NAMES).forEach { name ->
                    ChagokChip(text = name, colors = ChagokChipDefaults.white(), height = 34.dp)
                }
                val hiddenCount = placeNames.size - MAX_VISIBLE_PLACE_NAMES
                if (hiddenCount > 0) {
                    ChagokChip(
                        text = stringResource(R.string.feature_home_recording_more_places, hiddenCount),
                        colors = ChagokChipDefaults.white(),
                        height = 34.dp,
                    )
                }
            }
        }
    }
}

/** 위젯 머리: 로고 + "차곡차곡 쌓기" + 오른쪽 작은 글자 */
@Composable
private fun RecordWidgetHeader(meta: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ChagokTheme.colors.mint, RoundedCornerShape(11.dp)),
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
                text = stringResource(R.string.feature_home_record_title),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 20.sp, lineHeight = 28.sp),
                color = ChagokTheme.colors.ink,
            )
        }
        Text(
            text = meta,
            style = ChagokTheme.typography.caption.copy(fontSize = 15.sp),
            color = ChagokTheme.colors.inkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val START_BUTTON_RATIO = 184f / 236f

// 물결과 숨쉬기 값의 기준: 웹 데모 css/screens.css의 recWave, recHum
private const val WAVE_COUNT = 2
private const val WAVE_MILLIS = 3_200
private const val WAVE_START_SCALE = 0.96f
private const val WAVE_END_SCALE = 1.5f
private const val COMPACT_WAVE_END_SCALE = 1.36f
private const val WAVE_START_ALPHA = 0.42f
private const val WAVE_MID_ALPHA = 0.08f
private const val WAVE_FADE_POINT = 0.7f
private val WaveEasing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)
private const val HUM_MILLIS = 1_600
private const val HUM_SCALE = 1.04f
private const val MAX_VISIBLE_PLACE_NAMES = 2
