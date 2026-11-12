package com.chagokchagok.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 앱 공통 테마. 지금은 Material3 기본값을 그대로 쓴다.
 * 색·글꼴·모양은 UI 자료가 확정되면 이 파일에서 바꾼다.
 */
@Composable
fun ChagokTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
