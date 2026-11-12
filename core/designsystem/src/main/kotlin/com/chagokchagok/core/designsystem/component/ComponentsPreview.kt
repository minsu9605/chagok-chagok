package com.chagokchagok.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme

/**
 * 공통 부품 확인용 미리보기.
 * 기획 저장소 `04. 화면 설계 및 UI/images/00_B-Foundations.png`의 "부품" 칸과 나란히 놓고 비교한다.
 * 앱 화면에서는 쓰지 않는다.
 */
@Preview(name = "B안 부품", widthDp = 560, heightDp = 800)
@Composable
private fun ComponentsPreview() {
    ChagokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChagokTheme.colors.background)
                .padding(24.dp),
        ) {
            ChagokWidgetCard(modifier = Modifier.fillMaxSize()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "부품",
                        style = ChagokTheme.typography.cardTitle,
                        color = ChagokTheme.colors.ink,
                    )
                    ButtonSamples()
                    ChipSamples()
                    PassSample()
                    ChagokToast(title = "방금 다녀오신 ‘앞산 카페’ 어땠어요?")
                    ChagokProgressBar(progress = 0.45f)
                }
            }
        }
    }
}

@Composable
private fun ButtonSamples() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChagokButton(text = "기록 시작", onClick = {}, icon = ChagokIcons.Plus, elevated = true)
        ChagokButton(text = "닫기", onClick = {}, style = ChagokButtonStyle.Secondary)
        ChagokIconButton(
            icon = ChagokIcons.Mic,
            contentDescription = null,
            onClick = {},
            style = ChagokButtonStyle.Primary,
            size = 64.dp,
            iconSize = 28.dp,
            elevated = true,
        )
        ChagokIconButton(
            icon = ChagokIcons.Close,
            contentDescription = null,
            onClick = {},
            style = ChagokButtonStyle.White,
            size = 48.dp,
        )
    }
}

@Composable
private fun ChipSamples() {
    val relation = ChagokTheme.colors.relation
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChagokChip(
            text = "연인·부부",
            colors = ChagokChipDefaults.relation(relation.couple),
            icon = ChagokIcons.Heart,
        )
        ChagokChip(text = "좋았어요", colors = ChagokChipDefaults.mint(), icon = ChagokIcons.ThumbUp)
        ChagokChip(text = "별로예요", colors = ChagokChipDefaults.danger(), icon = ChagokIcons.ThumbDown)
        ChagokChip(text = "#치즈케이크맛집", colors = ChagokChipDefaults.lemon())
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChagokChip(text = "친구", colors = ChagokChipDefaults.relation(relation.friend), icon = ChagokIcons.Users)
        ChagokChip(text = "가족", colors = ChagokChipDefaults.relation(relation.family), icon = ChagokIcons.House)
        ChagokChip(text = "나 혼자", colors = ChagokChipDefaults.relation(relation.solo), icon = ChagokIcons.User)
        ChagokChip(text = "탑승 2명 감지", colors = ChagokChipDefaults.mint(), height = 36.dp)
    }
}

@Composable
private fun PassSample() {
    BoardingPassCard(
        palette = ChagokTheme.colors.relation.couple,
        icon = ChagokIcons.Heart,
        overline = "연인과 · 2026.10.12",
        title = "앞산 카페",
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            BoardingPassField(label = "거리", value = "약 2km", modifier = Modifier.width(200.dp))
            BoardingPassField(label = "남긴 평가", value = "좋았어요", icon = ChagokIcons.ThumbUp)
        }
        BoardingPassField(label = "해시태그") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChagokChip(text = "#치즈케이크맛집", colors = ChagokChipDefaults.lemon())
                ChagokChip(text = "#꾸덕함", colors = ChagokChipDefaults.lemon())
            }
        }
    }
}

/** 팝업 확인용 미리보기. 화면 05(관계 고르기)의 틀과 비교한다. */
@Preview(name = "B안 팝업", widthDp = 1280, heightDp = 690)
@Composable
private fun DialogPreview() {
    ChagokTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChagokTheme.colors.background),
        ) {
            ChagokDialog(onClose = {}) {
                ChagokChip(
                    text = "좌석 2명 감지",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ChagokChipDefaults.mint(),
                    icon = ChagokIcons.Users,
                    height = 44.dp,
                )
                Text(
                    text = "오늘 두 분이 함께하시네요!\n누구와 함께하는 드라이브인가요?",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = ChagokTheme.typography.screenTitle,
                    color = ChagokTheme.colors.ink,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChagokButton(text = "연인·부부", onClick = {}, modifier = Modifier.weight(1f))
                    ChagokButton(
                        text = "친구",
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        style = ChagokButtonStyle.Secondary,
                    )
                }
            }
        }
    }
}
