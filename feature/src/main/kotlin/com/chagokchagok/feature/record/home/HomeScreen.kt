package com.chagokchagok.feature.record.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.feature.R

/** 초기 설정 확인용 빈 홈 화면. 실제 홈 화면은 화면 구현 단계에서 채운다. */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.feature_home_title),
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    ChagokTheme {
        HomeScreen()
    }
}
