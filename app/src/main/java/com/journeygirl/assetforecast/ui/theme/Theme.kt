package com.journeygirl.assetforecast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Material3は未指定のパラメータ（primaryContainer等）をデフォルトの紫系パレットで埋めてしまうため、
// FABやボタン等が意図せず紫のままになる。使われ得る主要パラメータは全て明示的に指定する。
private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = OnAccentGreen,
    primaryContainer = AccentGreenContainer,
    onPrimaryContainer = OnAccentGreenContainer,
    secondary = AccentBlue,
    onSecondary = OnAccentBlue,
    secondaryContainer = AccentBlueContainer,
    onSecondaryContainer = OnAccentBlueContainer,
    tertiary = AccentBlue,
    onTertiary = OnAccentBlue,
    tertiaryContainer = AccentBlueContainer,
    onTertiaryContainer = OnAccentBlueContainer,
    background = BgDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnDarkMuted,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    error = GainRed,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = AccentGreenLight,
    onPrimary = Color.White,
    primaryContainer = AccentGreenContainerLight,
    onPrimaryContainer = OnAccentGreenContainerLight,
    secondary = AccentBlueLight,
    onSecondary = Color.White,
    secondaryContainer = AccentBlueContainerLight,
    onSecondaryContainer = OnAccentBlueContainerLight,
    tertiary = AccentBlueLight,
    onTertiary = Color.White,
    tertiaryContainer = AccentBlueContainerLight,
    onTertiaryContainer = OnAccentBlueContainerLight,
    background = BgLight,
    onBackground = OnLight,
    surface = SurfaceLight,
    onSurface = OnLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnLightMuted,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    error = GainRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun AssetforecastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 証券/資産管理アプリ風の配色（濃いグレー基調＋グリーン/ブルーのアクセント）を常に優先する。
    // 端末の壁紙に左右されるダイナミックカラーはブランドの一貫性を崩すため使わない。
    // 増減・実績線・予測線の色はユーザーが設定で変更できる（未設定ならテーマの初期色）。
    seriesColorChoices: SeriesColorChoices = SeriesColorChoices(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val seriesColors = SeriesColors(
        gain = resolveColorChoice(seriesColorChoices.gain, darkTheme, if (darkTheme) GainRed else GainRedLight),
        loss = resolveColorChoice(seriesColorChoices.loss, darkTheme, if (darkTheme) LossBlue else LossBlueLight),
        actual = resolveColorChoice(seriesColorChoices.actual, darkTheme, DefaultActualLine),
        forecast = resolveColorChoice(seriesColorChoices.forecast, darkTheme, DefaultForecastLine)
    )

    CompositionLocalProvider(LocalSeriesColors provides seriesColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
