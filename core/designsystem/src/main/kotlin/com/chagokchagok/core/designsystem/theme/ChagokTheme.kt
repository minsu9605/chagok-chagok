package com.chagokchagok.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * 앱 공통 테마 (민트 레몬).
 * 화면에서는 `ChagokTheme.colors.mint`, `ChagokTheme.typography.body`처럼 꺼내 쓴다.
 * Material 컴포넌트도 같은 색·글자를 쓰도록 Material 테마에도 같은 값을 넣는다.
 */
@Composable
fun ChagokTheme(content: @Composable () -> Unit) {
    val colors = ChagokColors()
    val typography = ChagokTypography()

    CompositionLocalProvider(
        LocalChagokColors provides colors,
        LocalChagokTypography provides typography,
        LocalChagokShapes provides ChagokShapes(),
        LocalChagokElevation provides ChagokElevation(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            content = content,
        )
    }
}

/** 테마 값을 꺼내는 곳. */
object ChagokTheme {
    val colors: ChagokColors
        @Composable
        @ReadOnlyComposable
        get() = LocalChagokColors.current

    val typography: ChagokTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalChagokTypography.current

    val shapes: ChagokShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalChagokShapes.current

    val elevation: ChagokElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalChagokElevation.current
}

private fun ChagokColors.toMaterialColorScheme() = lightColorScheme(
    primary = mint,
    onPrimary = onMint,
    primaryContainer = mintSoft,
    onPrimaryContainer = onMintSoft,
    secondary = mintInk,
    onSecondary = Color.White,
    secondaryContainer = lemon,
    onSecondaryContainer = onLemon,
    background = background,
    onBackground = ink,
    surface = card,
    onSurface = ink,
    surfaceVariant = fill,
    onSurfaceVariant = inkSecondary,
    outlineVariant = fill,
    error = danger,
    onError = Color.White,
    errorContainer = dangerSoft,
    onErrorContainer = dangerInk,
    scrim = scrim,
)

private fun ChagokTypography.toMaterialTypography() = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = display,
    headlineLarge = screenTitle,
    headlineMedium = screenTitle,
    headlineSmall = screenTitle,
    titleLarge = cardTitle,
    titleMedium = cardTitle,
    titleSmall = caption,
    bodyLarge = body,
    bodyMedium = caption,
    bodySmall = caption,
    labelLarge = label,
    labelMedium = label,
    labelSmall = label,
)
