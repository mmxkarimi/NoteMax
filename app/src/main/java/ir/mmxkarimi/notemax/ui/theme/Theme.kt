package ir.mmxkarimi.notemax.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ir.mmxkarimi.notemax.data.settings.ThemeMode

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
    outlineVariant = FallbackDarkOutlineVariant,
    scrim = FallbackDarkScrim,
    error = FallbackDarkError,
    onError = FallbackDarkOnError,
    errorContainer = FallbackDarkErrorContainer,
    onErrorContainer = FallbackDarkOnErrorContainer,
    inverseSurface = FallbackDarkInverseSurface,
    inverseOnSurface = FallbackDarkInverseOnSurface,
    inversePrimary = FallbackDarkInversePrimary,
    surfaceTint = FallbackDarkSurfaceTint,
    surfaceDim = FallbackDarkSurfaceDim,
    surfaceBright = FallbackDarkSurfaceBright,
    surfaceContainerLowest = FallbackDarkSurfaceContainerLowest,
    surfaceContainerLow = FallbackDarkSurfaceContainerLow,
    surfaceContainer = FallbackDarkSurfaceContainer,
    surfaceContainerHigh = FallbackDarkSurfaceContainerHigh,
    surfaceContainerHighest = FallbackDarkSurfaceContainerHighest
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
    outlineVariant = FallbackLightOutlineVariant,
    scrim = FallbackLightScrim,
    error = FallbackLightError,
    onError = FallbackLightOnError,
    errorContainer = FallbackLightErrorContainer,
    onErrorContainer = FallbackLightOnErrorContainer,
    inverseSurface = FallbackLightInverseSurface,
    inverseOnSurface = FallbackLightInverseOnSurface,
    inversePrimary = FallbackLightInversePrimary,
    surfaceTint = FallbackLightSurfaceTint,
    surfaceDim = FallbackLightSurfaceDim,
    surfaceBright = FallbackLightSurfaceBright,
    surfaceContainerLowest = FallbackLightSurfaceContainerLowest,
    surfaceContainerLow = FallbackLightSurfaceContainerLow,
    surfaceContainer = FallbackLightSurfaceContainer,
    surfaceContainerHigh = FallbackLightSurfaceContainerHigh,
    surfaceContainerHighest = FallbackLightSurfaceContainerHighest
  )

@Composable
fun NoteMaxTheme(
  themeMode: ThemeMode = ThemeMode.System,
  // Brand lime is locked by default; wallpaper colours are opt-in from Settings (Android 12+).
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
  }
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val activity = view.context.findActivity() ?: return@SideEffect
      val window = activity.window
      // Bar icon contrast follows the in-app theme (which may differ from the system theme).
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
      window.decorView.setBackgroundColor(colorScheme.background.toArgb())
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = NoteMaxShapes,
    content = content
  )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

/** True when the *app* surface is dark (follows the in-app theme setting, not only the system). */
@Composable
fun isAppDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
