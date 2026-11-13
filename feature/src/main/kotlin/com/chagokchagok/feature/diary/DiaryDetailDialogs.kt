package com.chagokchagok.feature.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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

/**
 * 다시 말하기 (화면 50). 새로 말하면 지금 한마디가 바뀐다.
 * 마이크로 듣지 못할 때를 위해 같은 자리를 눌러 글자로 적고 [저장]할 수도 있다.
 */
@Composable
internal fun RespeakDialog(
    respeak: RespeakUi,
    monthDayText: String,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circle = ChagokTheme.shapes.pill

    ChagokDialog(modifier = modifier, width = 720.dp) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChagokChip(
                text = stringResource(R.string.feature_diary_respeak),
                colors = ChagokChipDefaults.mint(),
                icon = ChagokIcons.Refresh,
                height = 34.dp,
            )
            Text(
                text = visitLabel(respeak.number, respeak.placeName, monthDayText),
                style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = ChagokTheme.colors.inkSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = stringResource(R.string.feature_diary_respeak_title),
            style = ChagokTheme.typography.screenTitle.copy(fontSize = 24.sp, lineHeight = 32.sp),
            color = ChagokTheme.colors.ink,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ChagokTheme.colors.fill, ChagokTheme.shapes.innerCard)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_diary_respeak_now),
                style = ChagokTheme.typography.label,
                color = ChagokTheme.colors.inkSecondary,
            )
            Text(
                text = stringResource(R.string.feature_memory_saved_memo_quote, respeak.currentMemo),
                style = ChagokTheme.typography.caption.copy(fontSize = 17.sp),
                color = ChagokTheme.colors.ink,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChagokTheme.colors.background, ChagokTheme.shapes.card)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .chagokShadow(ChagokTheme.elevation.level2, circle)
                    .background(ChagokTheme.colors.mint, circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChagokIcons.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = ChagokTheme.colors.onMint,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        if (respeak.listening) {
                            R.string.feature_memory_memo_input_label
                        } else {
                            R.string.feature_memory_memo_input_label_typing
                        },
                    ),
                    style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = ChagokTheme.colors.mintInk,
                )
                Box {
                    if (respeak.text.isEmpty()) {
                        Text(
                            text = stringResource(
                                if (respeak.listening) {
                                    R.string.feature_diary_respeak_placeholder
                                } else {
                                    R.string.feature_memory_memo_input_placeholder
                                },
                            ),
                            style = ChagokTheme.typography.body,
                            color = ChagokTheme.colors.inkSecondary,
                        )
                    }
                    BasicTextField(
                        value = respeak.text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = ChagokTheme.typography.body.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChagokTheme.colors.ink,
                        ),
                        cursorBrush = SolidColor(ChagokTheme.colors.mint),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSaveClick() }),
                        singleLine = true,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ChagokIcons.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChagokTheme.colors.mintInk,
            )
            Text(
                text = stringResource(R.string.feature_diary_respeak_note),
                style = ChagokTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = ChagokTheme.colors.inkSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChagokButton(
                text = stringResource(R.string.feature_diary_close),
                onClick = onCloseClick,
                modifier = Modifier.weight(1f),
                style = ChagokButtonStyle.Secondary,
                height = 60.dp,
            )
            ChagokButton(
                text = stringResource(R.string.feature_memory_memo_save),
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                height = 60.dp,
                enabled = respeak.text.isNotBlank(),
            )
        }
    }
}

/** 삭제 확인 창 (화면 52, 53): 휴지통, 제목, 설명, [취소 | 삭제]. */
@Composable
internal fun DiaryAlert(
    title: String,
    description: String,
    confirmText: String,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = ChagokTheme.shapes.alert

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChagokTheme.colors.scrim)
            // 덮개 뒤의 화면이 눌리지 않게 터치를 여기서 받는다.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .chagokShadow(ChagokTheme.elevation.level3, shape)
                .clip(shape)
                .background(ChagokTheme.colors.card),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.padding(start = 28.dp, top = 26.dp, end = 28.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(ChagokTheme.colors.dangerSoft, ChagokTheme.shapes.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ChagokIcons.Trash,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = ChagokTheme.colors.danger,
                    )
                }
                Text(
                    text = title,
                    style = ChagokTheme.typography.cardTitle.copy(fontSize = 21.sp, lineHeight = 30.sp),
                    color = ChagokTheme.colors.ink,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = description,
                    style = ChagokTheme.typography.caption.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = ChagokTheme.colors.inkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ChagokTheme.colors.fill),
            )
            Row(modifier = Modifier.height(64.dp)) {
                AlertButton(
                    text = stringResource(R.string.feature_diary_cancel),
                    color = ChagokTheme.colors.ink,
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(64.dp)
                        .background(ChagokTheme.colors.fill),
                )
                AlertButton(
                    text = confirmText,
                    color = ChagokTheme.colors.danger,
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AlertButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ChagokTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold),
            color = color,
        )
    }
}
