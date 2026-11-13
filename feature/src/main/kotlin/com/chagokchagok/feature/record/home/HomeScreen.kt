package com.chagokchagok.feature.record.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chagokchagok.core.designsystem.component.ChagokToast
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import com.chagokchagok.feature.record.relation.RelationPickerDialog
import kotlinx.coroutines.delay

/**
 * 메인 홈 (화면 02 기록 꺼짐, 10 기록 중).
 * 1차에서는 쌓기 위젯만 동작한다. 일기 보기·코스 짜기·기억 물어보기는 누르면 "준비 중이에요" 토스트만 띄운다.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onGoToNaviClick: () -> Unit = {},
    onRecordEnded: (sessionId: String) -> Unit = {},
    onRecordStarted: (autoStarted: Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnRecordStarted by rememberUpdatedState(onRecordStarted)

    // 기록을 시작하면 주행 화면으로 넘어간다. 시작 안내 토스트는 그 화면이 띄운다.
    LaunchedEffect(viewModel) {
        viewModel.recordStarted.collect { autoStarted -> currentOnRecordStarted(autoStarted) }
    }
    val currentOnRecordEnded by rememberUpdatedState(onRecordEnded)
    LaunchedEffect(viewModel) {
        viewModel.recordEnded.collect { sessionId -> currentOnRecordEnded(sessionId) }
    }

    HomeContent(
        uiState = uiState,
        actions = HomeActions(
            onStartRecordClick = viewModel::onStartRecordClick,
            onGoToNaviClick = onGoToNaviClick,
            onEndRecordClick = viewModel::onEndRecordClick,
            onRelationClick = viewModel::onRelationClick,
            onShowAllRelationsClick = viewModel::onShowAllRelationsClick,
            onBackToRecommendedClick = viewModel::onBackToRecommendedClick,
            onRelationPickerClose = viewModel::onRelationPickerClose,
        ),
        modifier = modifier,
    )
}

/** 홈 화면에서 일어나는 동작 묶음. */
private data class HomeActions(
    val onStartRecordClick: () -> Unit = {},
    val onGoToNaviClick: () -> Unit = {},
    val onEndRecordClick: () -> Unit = {},
    val onRelationClick: (RelationUi) -> Unit = {},
    val onShowAllRelationsClick: () -> Unit = {},
    val onBackToRecommendedClick: () -> Unit = {},
    val onRelationPickerClose: () -> Unit = {},
)

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    // 창을 어디에 얼마나 크게 띄울지는 시스템과 사용자가 정한다. 화면은 주어진 창 너비에 맞춰 배치를 고른다.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.background),
    ) {
        val layout = when {
            maxWidth < NARROW_MAX_WIDTH -> HomeLayout.Narrow
            maxWidth < COMPACT_MAX_WIDTH -> HomeLayout.Compact
            else -> HomeLayout.Wide
        }
        val compact = layout != HomeLayout.Wide

        // 준비 중인 위젯을 누를 때마다 1씩 올라간다. 값이 바뀌면 토스트를 다시 띄운다.
        var comingSoonClickCount by remember { mutableIntStateOf(0) }
        var comingSoonToastVisible by remember { mutableStateOf(false) }
        LaunchedEffect(comingSoonClickCount) {
            if (comingSoonClickCount > 0) {
                comingSoonToastVisible = true
                delay(COMING_SOON_TOAST_MILLIS)
                comingSoonToastVisible = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (compact) 24.dp else 32.dp,
                    vertical = if (compact) 16.dp else 22.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp),
        ) {
            HomeHeader(
                uiState = uiState,
                compact = compact,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp),
            ) {
                RecordWidget(
                    uiState = uiState,
                    compact = compact,
                    onStartRecordClick = actions.onStartRecordClick,
                    onGoToNaviClick = actions.onGoToNaviClick,
                    onEndRecordClick = actions.onEndRecordClick,
                    modifier = Modifier
                        .weight(RECORD_WEIGHT)
                        .fillMaxHeight(),
                )
                // 좁은 창(세로형)에서는 쌓기 위젯만 보인다. 나머지 위젯은 자리가 없어 뺀다.
                if (layout != HomeLayout.Narrow) {
                    FeatureWidgets(
                        diarySummary = uiState.diarySummary,
                        compact = compact,
                        onComingSoonClick = { comingSoonClickCount++ },
                        modifier = Modifier
                            .weight(OTHERS_WEIGHT)
                            .fillMaxHeight(),
                    )
                }
            }
        }

        val relationPicker = uiState.relationPicker
        if (relationPicker != null) {
            RelationPickerDialog(
                picker = relationPicker,
                onRelationClick = actions.onRelationClick,
                onShowAllClick = actions.onShowAllRelationsClick,
                onBackToRecommendedClick = actions.onBackToRecommendedClick,
                onCloseClick = actions.onRelationPickerClose,
            )
        }

        AnimatedVisibility(
            visible = comingSoonToastVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ChagokToast(title = stringResource(R.string.feature_home_coming_soon_toast))
        }
    }
}

@Composable
private fun RecordWidget(
    uiState: HomeUiState,
    compact: Boolean,
    onStartRecordClick: () -> Unit,
    onGoToNaviClick: () -> Unit,
    onEndRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recording = uiState.recording
    if (recording == null) {
        RecordIdleWidget(
            todayPlaceCount = uiState.todayPlaceCount,
            lastRecord = uiState.lastRecord,
            compact = compact,
            onStartRecordClick = onStartRecordClick,
            modifier = modifier,
        )
    } else {
        RecordActiveWidget(
            recording = recording,
            compact = compact,
            onGoToNaviClick = onGoToNaviClick,
            onEndRecordClick = onEndRecordClick,
            modifier = modifier,
        )
    }
}

/** 위에 일기 보기·코스 짜기, 아래에 기억 물어보기. 2차에서 만들 기능이라 지금은 누르면 준비 중 토스트만 띄운다. */
@Composable
private fun FeatureWidgets(
    diarySummary: DiarySummaryUi?,
    compact: Boolean,
    onComingSoonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = if (compact) 12.dp else 16.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            DiaryWidget(
                summary = diarySummary,
                onClick = onComingSoonClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CourseWidget(
                compact = compact,
                onClick = onComingSoonClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        AskWidget(
            compact = compact,
            onClick = onComingSoonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 190.dp else 216.dp),
        )
    }
}

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    compact: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = uiState.dateText,
            style = ChagokTheme.typography.caption.copy(fontSize = if (compact) 17.sp else 18.sp),
            color = ChagokTheme.colors.inkSecondary,
        )
        val recording = uiState.recording
        if (recording == null) {
            SeatStatusChip(seatCount = uiState.seatCount)
        } else {
            RecordingStatusChip(relation = recording.relation)
        }
    }
}

/** 기록 꺼짐: "탑승 2명 감지" */
@Composable
private fun SeatStatusChip(seatCount: Int) {
    val shape = ChagokTheme.shapes.pill

    Row(
        modifier = Modifier
            .chagokShadow(ChagokTheme.elevation.level1, shape)
            .height(48.dp)
            .clip(shape)
            .background(ChagokTheme.colors.card)
            .padding(start = 14.dp, end = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ChagokIcons.Users,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ChagokTheme.colors.mintInk,
        )
        Text(
            text = stringResource(R.string.feature_home_seat_detected, seatCount),
            style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.ink,
        )
    }
}

/** 기록 중: "연인 모드 기록 중" */
@Composable
private fun RecordingStatusChip(relation: RelationUi) {
    val palette = relation.palette

    Row(
        modifier = Modifier
            .height(48.dp)
            .background(palette.soft, ChagokTheme.shapes.pill)
            .padding(start = 14.dp, end = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 기록 중 점: 관계 색 점 + 옅은 테두리
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(palette.band.copy(alpha = 0.25f), ChagokTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(palette.band, ChagokTheme.shapes.pill),
            )
        }
        Text(
            text = stringResource(
                R.string.feature_home_status_recording,
                stringResource(relation.modeLabelRes),
            ),
            style = ChagokTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
            color = palette.ink,
        )
    }
}

/**
 * 창 너비에 따른 홈 배치.
 * 기준 크기: 전체 화면 1280, 분할 화면 큰 창 850, 작은 창 420 (화면 설계 개요 1장).
 */
private enum class HomeLayout {
    /** 시안의 전체 화면 배치 (화면 02, 10) */
    Wide,

    /** 분할 화면 큰 창. 같은 구성에서 여백을 줄이고 예시 문장을 뺀다. */
    Compact,

    /** 분할 화면 작은 창(세로형). 쌓기 위젯만 보인다. */
    Narrow,
}

private const val COMING_SOON_TOAST_MILLIS = 2_000L

private val NARROW_MAX_WIDTH = 600.dp
private val COMPACT_MAX_WIDTH = 1000.dp

// 시안의 열 너비 비율: 쌓기 500, 나머지 700 (간격 제외). 큰 창에서도 같은 비율을 쓴다.
private const val RECORD_WEIGHT = 500f
private const val OTHERS_WEIGHT = 700f

private val PreviewIdleState = HomeUiState(
    dateText = "11월 9일 월요일",
    seatCount = 2,
    todayPlaceCount = 0,
    lastRecord = LastRecordUi(dateText = "10월 24일", relation = RelationUi.FAMILY, placeCount = 2),
    diarySummary = DiarySummaryUi(diaryCount = 10, pinCount = 20),
)

private val PreviewRecordingState = HomeUiState(
    dateText = "11월 9일 월요일",
    seatCount = 2,
    diarySummary = DiarySummaryUi(diaryCount = 10, pinCount = 20),
    recording = RecordingUi(
        relation = RelationUi.COUPLE,
        seatCount = 2,
        startedAtText = "09:40",
        placeNames = listOf("교동 뭉티기"),
    ),
)

@Composable
private fun HomePreview(uiState: HomeUiState) {
    ChagokTheme {
        HomeContent(uiState = uiState, actions = HomeActions())
    }
}

@Preview(name = "02 홈 · 기록 꺼짐 · 전체 화면", widthDp = 1280, heightDp = 690)
@Composable
private fun HomeIdleWidePreview() = HomePreview(PreviewIdleState)

@Preview(name = "10 홈 · 기록 중 · 전체 화면", widthDp = 1280, heightDp = 690)
@Composable
private fun HomeRecordingWidePreview() = HomePreview(PreviewRecordingState)

@Preview(name = "02 홈 · 기록 꺼짐 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun HomeIdleCompactPreview() = HomePreview(PreviewIdleState)

@Preview(name = "10 홈 · 기록 중 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun HomeRecordingCompactPreview() = HomePreview(PreviewRecordingState)

@Preview(name = "02 홈 · 기록 꺼짐 · 작은 창", widthDp = 426, heightDp = 605)
@Composable
private fun HomeIdleNarrowPreview() = HomePreview(PreviewIdleState)

@Preview(name = "10 홈 · 기록 중 · 작은 창", widthDp = 426, heightDp = 605)
@Composable
private fun HomeRecordingNarrowPreview() = HomePreview(PreviewRecordingState)
