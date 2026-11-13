package com.chagokchagok.feature.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * 지금 키보드로 글자를 입력해도 되는지. 주행 중에는 false다.
 * 앱 맨 위에서 차량의 주행 제한을 받아 넣어 준다. 넣지 않으면(미리보기 등) 허용으로 본다.
 */
val LocalKeyboardAllowed = compositionLocalOf { true }

/**
 * 글자 입력 칸이 쓸 값. 키보드를 쓸 수 없게 되면 떠 있던 키보드를 내리고 입력 칸에서 초점을 뺀다.
 * 입력 칸은 이 값으로 `enabled`를 정해, 주행 중에는 눌러도 키보드가 뜨지 않게 한다.
 */
@Composable
internal fun rememberKeyboardAllowed(): Boolean {
    val allowed = LocalKeyboardAllowed.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(allowed) {
        if (!allowed) {
            keyboard?.hide()
            focusManager.clearFocus(force = true)
        }
    }
    return allowed
}
