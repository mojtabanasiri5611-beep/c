package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = Neutral50,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal700,
    secondary = Neutral700,
    onSecondary = Neutral50,
    secondaryContainer = Neutral100,
    onSecondaryContainer = Neutral800,
    tertiary = InfoLight,
    onTertiary = OnInfoLight,
    tertiaryContainer = InfoContainerLight,
    onTertiaryContainer = OnInfoContainerLight,
    error = DangerLight,
    onError = OnDangerLight,
    errorContainer = DangerContainerLight,
    onErrorContainer = OnDangerContainerLight,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Neutral50,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    outline = Neutral300,
    outlineVariant = Neutral200,
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral100,
    inversePrimary = Teal300,
    scrim = Neutral950
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal400,
    onPrimary = Neutral950,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal100,
    secondary = Neutral300,
    onSecondary = Neutral800,
    secondaryContainer = Neutral700,
    onSecondaryContainer = Neutral100,
    tertiary = InfoDark,
    onTertiary = OnInfoDark,
    tertiaryContainer = InfoContainerDark,
    onTertiaryContainer = OnInfoContainerDark,
    error = DangerDark,
    onError = OnDangerDark,
    errorContainer = DangerContainerDark,
    onErrorContainer = OnDangerContainerDark,
    background = Neutral950,
    onBackground = Neutral100,
    surface = Neutral900,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral400,
    outline = Neutral500,
    outlineVariant = Neutral700,
    inverseSurface = Neutral100,
    inverseOnSurface = Neutral800,
    inversePrimary = Teal600,
    scrim = Neutral950
)

@Composable
fun ChequeManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }
    val currentConfig = LocalConfiguration.current
    val fixedConfig = remember(currentConfig) {
        android.content.res.Configuration(currentConfig).apply { fontScale = 1.0f }
    }

    CompositionLocalProvider(
        LocalConfiguration provides fixedConfig,
        LocalDensity provides fixedFontDensity,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ChequeManagerTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
