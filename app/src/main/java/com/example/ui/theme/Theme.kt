package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark Color Scheme with #c6f135 as primary fallback
private val DarkColorScheme =
  darkColorScheme(
    primary = FallbackDarkPrimary,
    onPrimary = FallbackDarkOnPrimary,
    primaryContainer = FallbackDarkPrimaryContainer,
    onPrimaryContainer = FallbackDarkOnPrimaryContainer,
    secondary = FallbackDarkSecondary,
    onSecondary = FallbackDarkOnSecondary,
    secondaryContainer = FallbackDarkSecondaryContainer,
    onSecondaryContainer = FallbackDarkOnSecondaryContainer,
    tertiary = FallbackDarkTertiary,
    onTertiary = FallbackDarkOnTertiary,
    tertiaryContainer = FallbackDarkTertiaryContainer,
    onTertiaryContainer = FallbackDarkOnTertiaryContainer,
    background = FallbackDarkBackground,
    onBackground = FallbackDarkOnBackground,
    surface = FallbackDarkSurface,
    onSurface = FallbackDarkOnSurface,
    surfaceVariant = FallbackDarkSurfaceVariant,
    onSurfaceVariant = FallbackDarkOnSurfaceVariant,
    outline = FallbackDarkOutline,
    surfaceContainer = FallbackDarkSurfaceContainer,
    surfaceContainerHigh = FallbackDarkSurfaceContainerHigh
  )

// Light Color Scheme with #c6f135 as primary fallback
private val LightColorScheme =
  lightColorScheme(
    primary = FallbackLightPrimary,
    onPrimary = FallbackLightOnPrimary,
    primaryContainer = FallbackLightPrimaryContainer,
    onPrimaryContainer = FallbackLightOnPrimaryContainer,
    secondary = FallbackLightSecondary,
    onSecondary = FallbackLightOnSecondary,
    secondaryContainer = FallbackLightSecondaryContainer,
    onSecondaryContainer = FallbackLightOnSecondaryContainer,
    tertiary = FallbackLightTertiary,
    onTertiary = FallbackLightOnTertiary,
    tertiaryContainer = FallbackLightTertiaryContainer,
    onTertiaryContainer = FallbackLightOnTertiaryContainer,
    background = FallbackLightBackground,
    onBackground = FallbackLightOnBackground,
    surface = FallbackLightSurface,
    onSurface = FallbackLightOnSurface,
    surfaceVariant = FallbackLightSurfaceVariant,
    onSurfaceVariant = FallbackLightOnSurfaceVariant,
    outline = FallbackLightOutline,
    surfaceContainer = FallbackLightSurfaceContainer,
    surfaceContainerHigh = FallbackLightSurfaceContainerHigh
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Material Design 3 Dynamic color is supported on Android 12+ (API 31+)
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
