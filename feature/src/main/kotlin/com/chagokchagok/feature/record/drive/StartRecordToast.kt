package com.chagokchagok.feature.record.drive

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chagokchagok.core.designsystem.component.ChagokToast
import com.chagokchagok.feature.R
import com.chagokchagok.feature.common.RelationUi
import com.chagokchagok.feature.common.icon
import com.chagokchagok.feature.common.palette

/**
 * 기록 시작 안내 토스트 (화면 08, 08-1, 09). 관계 아이콘과 색으로 보여 준다.
 *
 * @param autoStarted 좌석 1명이라 묻지 않고 바로 시작했으면 true
 * @param guided 갈 곳이 정해진 채로 시작했으면 true
 */
@Composable
internal fun StartRecordToast(
    relation: RelationUi,
    autoStarted: Boolean,
    guided: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChagokToast(
        title = if (relation == RelationUi.SOLO) {
            stringResource(R.string.feature_home_start_toast_title_solo)
        } else {
            stringResource(R.string.feature_home_start_toast_title, stringResource(relation.withLabelRes))
        },
        modifier = modifier,
        message = stringResource(
            when {
                guided -> R.string.feature_home_start_toast_message_guided
                autoStarted -> R.string.feature_home_start_toast_message_auto
                else -> R.string.feature_home_start_toast_message
            },
        ),
        icon = relation.icon,
        iconContainerColor = relation.palette.soft,
        iconColor = relation.palette.ink,
        onClick = onClick,
    )
}
