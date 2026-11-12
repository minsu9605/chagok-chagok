package com.chagokchagok.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 테마 값 확인용 미리보기.
 * 기획 저장소 `04. 화면 설계 및 UI/images/00_B-Foundations.png`와 나란히 놓고 비교한다.
 * 앱 화면에서는 쓰지 않는다.
 */
@Preview(name = "B안 디자인 기준", widthDp = 1280, heightDp = 800)
@Composable
private fun FoundationsPreview() {
    ChagokTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(ChagokTheme.colors.background)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            FoundationPanel(title = "색") { ColorSamples() }
            FoundationPanel(title = "글자") { TypographySamples() }
            FoundationPanel(title = "모양 · 그림자") { ShapeAndShadowSamples() }
        }
    }
}

@Composable
private fun RowScope.FoundationPanel(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(ChagokTheme.colors.card, ChagokTheme.shapes.widget)
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, style = ChagokTheme.typography.cardTitle, color = ChagokTheme.colors.ink)
        content()
    }
}

@Composable
private fun ColorSamples() {
    val colors = ChagokTheme.colors
    SwatchRow(
        "바탕" to colors.background,
        "카드" to colors.card,
        "채움" to colors.fill,
        "글자" to colors.ink,
        "보조 글자" to colors.inkSecondary,
    )
    SwatchRow(
        "민트" to colors.mint,
        "민트 글자" to colors.mintInk,
        "연한 민트" to colors.mintSoft,
        "레몬" to colors.lemon,
        "경고" to colors.danger,
    )
    RelationRow(name = "연인·부부", palette = colors.relation.couple)
    RelationRow(name = "친구", palette = colors.relation.friend)
    RelationRow(name = "가족", palette = colors.relation.family)
    RelationRow(name = "나 혼자", palette = colors.relation.solo)
}

@Composable
private fun SwatchRow(vararg swatches: Pair<String, Color>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        swatches.forEach { (name, color) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 48.dp)
                        .background(ChagokTheme.colors.fill, ChagokTheme.shapes.innerCard)
                        .padding(1.dp)
                        .background(color, ChagokTheme.shapes.innerCard),
                )
                Text(text = name, style = ChagokTheme.typography.label, color = ChagokTheme.colors.ink)
            }
        }
    }
}

@Composable
private fun RelationRow(name: String, palette: RelationPalette) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 36.dp)
                .background(palette.band, ChagokTheme.shapes.innerCard),
        )
        Box(
            modifier = Modifier
                .height(30.dp)
                .background(palette.soft, ChagokTheme.shapes.pill)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = name, style = ChagokTheme.typography.label, color = palette.ink)
        }
    }
}

@Composable
private fun TypographySamples() {
    val typography = ChagokTheme.typography
    TypeSample(text = "차곡차곡", spec = "큰 제목 40 · 800", style = typography.display)
    TypeSample(text = "오늘의 드라이브", spec = "화면 제목 30 · 800", style = typography.screenTitle)
    TypeSample(text = "앞산 카페", spec = "카드 제목 24 · 800", style = typography.cardTitle)
    TypeSample(text = "누르면 탑승 인원을 확인해요", spec = "본문 18 · 500", style = typography.body)
    TypeSample(text = "약 2km · 10월 12일", spec = "보조 16 · 600", style = typography.caption)
    TypeSample(text = "근처에 남긴 기억", spec = "라벨 14 · 800", style = typography.label)

    // 글꼴 적용 확인용: 같은 문장을 앱 글꼴과 시스템 글꼴로 나란히 보여 준다. 두 줄의 글자 모양이 달라야 한다.
    TypeSample(
        text = "차곡차곡 Drive 2026",
        spec = "↑ 앱 글꼴 (Asta Sans)",
        style = typography.screenTitle,
    )
    TypeSample(
        text = "차곡차곡 Drive 2026",
        spec = "↑ 시스템 글꼴 (비교용)",
        style = typography.screenTitle.copy(fontFamily = FontFamily.Default),
    )
}

@Composable
private fun TypeSample(text: String, spec: String, style: TextStyle) {
    Column {
        Text(text = text, style = style, color = ChagokTheme.colors.ink)
        Text(text = spec, style = ChagokTheme.typography.label, color = ChagokTheme.colors.inkSecondary)
    }
}

@Composable
private fun ShapeAndShadowSamples() {
    val shapes = ChagokTheme.shapes
    val elevation = ChagokTheme.elevation
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ShapeSample(name = "위젯 36", shape = shapes.widget)
        ShapeSample(name = "카드 28", shape = shapes.card)
        ShapeSample(name = "안쪽 22", shape = shapes.innerCard)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ShapeSample(name = "팝업 40", shape = shapes.dialog)
        ShapeSample(name = "토스트 24", shape = shapes.toast)
        ShapeSample(name = "알약", shape = shapes.pill)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChagokTheme.colors.background, shapes.card)
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ShadowSample(name = "L1", layers = elevation.level1)
        ShadowSample(name = "L2", layers = elevation.level2)
        ShadowSample(name = "L3", layers = elevation.level3)
    }
}

@Composable
private fun ShapeSample(name: String, shape: Shape) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 104.dp, height = 72.dp)
                .background(ChagokTheme.colors.mintSoft, shape),
        )
        Text(text = name, style = ChagokTheme.typography.label, color = ChagokTheme.colors.ink)
    }
}

@Composable
private fun RowScope.ShadowSample(name: String, layers: List<ShadowLayer>) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .chagokShadow(layers, ChagokTheme.shapes.innerCard)
            .background(ChagokTheme.colors.card, ChagokTheme.shapes.innerCard),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name, style = ChagokTheme.typography.label, color = ChagokTheme.colors.ink)
    }
}
