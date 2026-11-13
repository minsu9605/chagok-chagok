package com.chagokchagok.feature.record.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.chagokchagok.core.designsystem.component.ChagokWidgetCard
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.chagokShadow
import com.chagokchagok.feature.R
import com.chagokchagok.feature.ask.AskActions
import com.chagokchagok.feature.ask.AskAnnouncement
import com.chagokchagok.feature.ask.AskPanel
import com.chagokchagok.feature.ask.AskStage
import com.chagokchagok.feature.ask.AskViewModel
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette
import kotlinx.coroutines.delay

/**
 * 주행 중 화면 (화면 11).
 *
 * 시안은 순정 내비가 큰 창, 이 앱의 미니 패널이 작은 창에 뜨는 모습이다.
 * 창 배치는 앱이 정할 수 없으므로, 창이 넓으면 왼쪽에 내비 자리를 비워 두고 오른쪽에 미니 패널을 놓는다.
 * 창이 좁으면(작은 창) 미니 패널만 보인다.
 *
 * @param justStarted 방금 기록을 시작해서 왔으면 true. 시작 안내 토스트를 띄운다 (화면 08, 09)
 * @param autoStarted 좌석 1명이라 묻지 않고 시작했으면 true
 * @param guided 갈 곳이 정해진 채로 시작했으면 true (화면 08-1)
 * @param onRecordEnded [기록 종료]로 기록을 끝냈을 때. 끝난 기록의 id를 넘긴다
 * @param onNotRecording 기록 중이 아닌데 이 화면에 있을 때
 */
@Composable
fun DriveScreen(
    justStarted: Boolean,
    autoStarted: Boolean,
    onHomeClick: () -> Unit,
    onRecordEnded: (sessionId: String) -> Unit,
    onNotRecording: () -> Unit,
    modifier: Modifier = Modifier,
    guided: Boolean = false,
    viewModel: DriveViewModel = hiltViewModel(),
    askViewModel: AskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val askUiState by askViewModel.uiState.collectAsStateWithLifecycle()

    // 답으로 나온 곳으로 안내를 고르면 내비 화면을 부른다. 장소에 좌표가 없어 목적지를 넣지는 못한다.
    LaunchedEffect(askViewModel) {
        askViewModel.guideRequests.collect { viewModel.onEnter() }
    }
    AskAnnouncement(stage = askUiState.stage, onAnswerShown = askViewModel::onAnswerShown)

    LaunchedEffect(Unit) { viewModel.onEnter() }
    LaunchedEffect(uiState.loaded, uiState.relation, uiState.endedSessionId) {
        val endedSessionId = uiState.endedSessionId
        when {
            endedSessionId != null -> onRecordEnded(endedSessionId)
            uiState.loaded && uiState.relation == null -> onNotRecording()
        }
    }

    DriveContent(
        uiState = uiState,
        justStarted = justStarted,
        autoStarted = autoStarted,
        guided = guided,
        askStage = askUiState.stage,
        askActions = AskActions(
            onQuestionChange = askViewModel::onQuestionChange,
            onQuestionSubmit = askViewModel::onQuestionSubmit,
            onExampleClick = askViewModel::onExampleClick,
            onFreshClick = askViewModel::onFreshClick,
            onGuideClick = askViewModel::onGuideClick,
            onClose = askViewModel::onClose,
        ),
        onVoiceClick = askViewModel::onOpen,
        onHomeClick = onHomeClick,
        onEndRecordClick = viewModel::onEndRecordClick,
        onDevArriveClick = viewModel::onDevArriveClick,
        onDevRestartClick = viewModel::onDevRestartClick,
        modifier = modifier,
    )
}

@Composable
private fun DriveContent(
    uiState: DriveUiState,
    justStarted: Boolean,
    autoStarted: Boolean,
    onHomeClick: () -> Unit,
    onEndRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
    guided: Boolean = false,
    askStage: AskStage? = null,
    askActions: AskActions = AskActions(),
    onVoiceClick: () -> Unit = {},
    onDevArriveClick: () -> Unit = {},
    onDevRestartClick: () -> Unit = {},
) {
    val relation = uiState.relation ?: return

    var startToastVisible by rememberSaveable { mutableStateOf(justStarted) }
    LaunchedEffect(Unit) {
        if (startToastVisible) {
            delay(START_TOAST_MILLIS)
            startToastVisible = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.background),
    ) {
        val panelOnly = maxWidth < PANEL_ONLY_MAX_WIDTH

        Row(modifier = Modifier.fillMaxSize()) {
            if (!panelOnly) {
                NaviPlaceholder(
                    nextDevPlaceName = uiState.nextDevPlaceName,
                    onDevArriveClick = onDevArriveClick,
                    onDevRestartClick = onDevRestartClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                )
            }
            val panelModifier = if (panelOnly) Modifier.fillMaxSize() else Modifier.width(PANEL_WIDTH).fillMaxHeight()
            if (askStage != null) {
                // 기억 물어보기는 미니 패널 자리를 차지한다 (화면 12~18).
                AskPanel(
                    stage = askStage,
                    scopeRelation = relation,
                    actions = askActions,
                    modifier = panelModifier,
                )
            } else {
                MiniPanel(
                    relation = relation,
                    placeNames = uiState.placeNames,
                    pendingPlaceName = uiState.pendingPlaceName,
                    onVoiceClick = onVoiceClick,
                    onHomeClick = onHomeClick,
                    onEndRecordClick = onEndRecordClick,
                    modifier = panelModifier,
                )
            }
        }

        val toastModifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(24.dp)
        if (startToastVisible) {
            StartRecordToast(
                relation = relation,
                autoStarted = autoStarted,
                guided = guided,
                onClick = { startToastVisible = false },
                modifier = toastModifier,
            )
        }
    }
}

/**
 * 순정 내비가 뜰 자리. 이 앱은 지도를 그리지 않는다.
 * [개발자용] 내비 도착과 시동 다시 켬을 흉내 내는 버튼을 아래에 둔다. 실제 내비 연결을 확인하면 삭제한다.
 */
@Composable
private fun NaviPlaceholder(
    nextDevPlaceName: String,
    onDevArriveClick: () -> Unit,
    onDevRestartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(ChagokTheme.colors.fill, RoundedCornerShape(20.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ChagokIcons.Navigation,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = ChagokTheme.colors.inkSecondary,
        )
        Text(
            text = stringResource(R.string.feature_drive_navi_placeholder_title),
            style = ChagokTheme.typography.cardTitle.copy(fontSize = 22.sp),
            color = ChagokTheme.colors.ink,
        )
        Text(
            text = stringResource(R.string.feature_drive_navi_placeholder_message),
            style = ChagokTheme.typography.caption,
            color = ChagokTheme.colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.feature_drive_dev_title),
            style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
            color = ChagokTheme.colors.inkSecondary,
        )
        ChagokButton(
            text = stringResource(R.string.feature_drive_dev_arrive, nextDevPlaceName),
            onClick = onDevArriveClick,
            modifier = Modifier.width(260.dp),
            style = ChagokButtonStyle.White,
            height = 52.dp,
            icon = ChagokIcons.Pin,
        )
        ChagokButton(
            text = stringResource(R.string.feature_drive_dev_restart),
            onClick = onDevRestartClick,
            modifier = Modifier.width(260.dp),
            style = ChagokButtonStyle.White,
            height = 52.dp,
            icon = ChagokIcons.Clock,
        )
    }
}

/** 미니 패널: 기록 상태, 오늘 다녀온 곳, 음성 질문, 홈·종료 버튼. */
@Composable
private fun MiniPanel(
    relation: RelationUi,
    placeNames: List<String>,
    pendingPlaceName: String?,
    onVoiceClick: () -> Unit,
    onHomeClick: () -> Unit,
    onEndRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiveDot()
                Text(
                    text = stringResource(R.string.feature_drive_recording),
                    style = ChagokTheme.typography.cardTitle.copy(fontSize = 19.sp, lineHeight = 26.sp),
                    color = ChagokTheme.colors.ink,
                )
            }
            ChagokChip(
                text = stringResource(relation.modeLabelRes),
                colors = ChagokChipDefaults.relation(relation.palette),
                icon = relation.icon,
                height = 36.dp,
            )
        }
        TodayPlacesCard(placeNames = placeNames, pendingPlaceName = pendingPlaceName)
        VoiceAskButton(
            onClick = onVoiceClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChagokButton(
                text = stringResource(R.string.feature_drive_go_home),
                onClick = onHomeClick,
                modifier = Modifier.weight(1f),
                style = ChagokButtonStyle.White,
                icon = ChagokIcons.House,
            )
            ChagokButton(
                text = stringResource(R.string.feature_home_recording_end),
                onClick = onEndRecordClick,
                modifier = Modifier.weight(1f),
                style = ChagokButtonStyle.White,
                icon = ChagokIcons.Stop,
            )
        }
    }
}

/** 기록 중임을 알리는 초록 점. */
@Composable
private fun LiveDot() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(ChagokTheme.colors.mint.copy(alpha = 0.3f), ChagokTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(ChagokTheme.colors.mint, ChagokTheme.shapes.pill),
        )
    }
}

/** "오늘 다녀온 곳 / 1곳 · 교동 뭉티기 외 1곳" */
@Composable
private fun TodayPlacesCard(placeNames: List<String>, pendingPlaceName: String?) {
    val firstName = placeNames.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.card, RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ChagokIcons.Pin,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = stringResource(R.string.feature_home_recording_today_label),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Text(
            text = when {
                firstName == null -> stringResource(R.string.feature_drive_today_empty)
                placeNames.size == 1 -> stringResource(R.string.feature_drive_today_one, firstName)
                else -> stringResource(
                    R.string.feature_drive_today_many,
                    placeNames.size,
                    firstName,
                    placeNames.size - 1,
                )
            },
            style = ChagokTheme.typography.cardTitle.copy(
                fontSize = if (firstName == null) 19.sp else 22.sp,
                lineHeight = 28.sp,
            ),
            color = ChagokTheme.colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (pendingPlaceName != null) {
            Text(
                text = stringResource(R.string.feature_drive_pending, pendingPlaceName),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.mintInk,
            )
        }
    }
}

/** 큰 [음성 질문하기]. 시안 크기는 바깥 152, 안쪽 116. */
@Composable
private fun VoiceAskButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val circle = ChagokTheme.shapes.pill

    ChagokWidgetCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(152.dp)
                    .background(ChagokTheme.colors.mintSoft, circle),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .chagokShadow(ChagokTheme.elevation.level2, circle)
                        .background(ChagokTheme.colors.mint, circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ChagokIcons.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = ChagokTheme.colors.onMint,
                    )
                }
            }
            Text(
                text = stringResource(R.string.feature_drive_voice_ask),
                style = ChagokTheme.typography.cardTitle.copy(fontSize = 26.sp, lineHeight = 34.sp),
                color = ChagokTheme.colors.ink,
            )
            Text(
                text = stringResource(R.string.feature_drive_voice_ask_hint),
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
    }
}

private const val START_TOAST_MILLIS = 4_000L

// 이 너비보다 좁으면 작은 창으로 보고 미니 패널만 보인다.
private val PANEL_ONLY_MAX_WIDTH = 600.dp

// 시안의 작은 창 너비는 420. 큰 창(841) 안에서는 내비 자리를 남기려고 조금 줄인다.
private val PANEL_WIDTH = 380.dp

@Preview(name = "11 주행 중 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DriveBigWindowPreview() = ChagokTheme {
    DriveContent(
        uiState = DriveUiState(loaded = true, relation = RelationUi.COUPLE, placeNames = listOf("교동 뭉티기")),
        justStarted = false,
        autoStarted = false,
        onHomeClick = {},
        onEndRecordClick = {},
    )
}

@Preview(name = "08 기록 시작 안내 · 큰 창", widthDp = 841, heightDp = 605)
@Composable
private fun DriveStartToastPreview() = ChagokTheme {
    DriveContent(
        uiState = DriveUiState(loaded = true, relation = RelationUi.COUPLE),
        justStarted = true,
        autoStarted = false,
        onHomeClick = {},
        onEndRecordClick = {},
    )
}

@Preview(name = "11 주행 중 · 작은 창", widthDp = 420, heightDp = 606)
@Composable
private fun DriveSmallWindowPreview() = ChagokTheme {
    DriveContent(
        uiState = DriveUiState(
            loaded = true,
            relation = RelationUi.FAMILY,
            placeNames = listOf("교동 뭉티기", "앞산 카페"),
        ),
        justStarted = false,
        autoStarted = false,
        onHomeClick = {},
        onEndRecordClick = {},
    )
}
