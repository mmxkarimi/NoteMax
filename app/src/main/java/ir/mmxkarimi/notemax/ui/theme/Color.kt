package ir.mmxkarimi.notemax.ui.theme

import androidx.compose.ui.graphics.Color

// Expressive Lime Fallback Color requested by user
val ExpressiveLime = Color(0xFFC6F135)
val ExpressiveLimeDark = Color(0xFF9EBE1C)
val ExpressiveLimeContainer = Color(0xFFE2F97D)
val OnExpressiveLimeContainer = Color(0xFF2C3900)

// Fallback Light Color Palette based on #c6f135
val FallbackLightPrimary = Color(0xFF4C6700)
val FallbackLightOnPrimary = Color(0xFFFFFFFF)
val FallbackLightPrimaryContainer = Color(0xFFC6F135)
val FallbackLightOnPrimaryContainer = Color(0xFF141F00)

val FallbackLightSecondary = Color(0xFF5A6147)
val FallbackLightOnSecondary = Color(0xFFFFFFFF)
val FallbackLightSecondaryContainer = Color(0xFFDEE6C5)
val FallbackLightOnSecondaryContainer = Color(0xFF171E09)

val FallbackLightTertiary = Color(0xFF386663)
val FallbackLightOnTertiary = Color(0xFFFFFFFF)
val FallbackLightTertiaryContainer = Color(0xFFBBECE7)
val FallbackLightOnTertiaryContainer = Color(0xFF00201E)

val FallbackLightBackground = Color(0xFFFAFBF1)
val FallbackLightOnBackground = Color(0xFF1A1C16)
val FallbackLightSurface = Color(0xFFFAFBF1)
val FallbackLightOnSurface = Color(0xFF1A1C16)
val FallbackLightSurfaceVariant = Color(0xFFE2E4D4)
val FallbackLightOnSurfaceVariant = Color(0xFF45483C)
val FallbackLightOutline = Color(0xFF75796B)
val FallbackLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val FallbackLightSurfaceContainerLow = Color(0xFFF7F8EE)
val FallbackLightSurfaceContainer = Color(0xFFF1F2E8)
val FallbackLightSurfaceContainerHigh = Color(0xFFEBEDE2)
val FallbackLightSurfaceContainerHighest = Color(0xFFE5E7DC)

// Fallback Dark Color Palette based on #c6f135
val FallbackDarkPrimary = Color(0xFFC6F135)
val FallbackDarkOnPrimary = Color(0xFF263500)
val FallbackDarkPrimaryContainer = Color(0xFF394E00)
val FallbackDarkOnPrimaryContainer = Color(0xFFE2F97D)

val FallbackDarkSecondary = Color(0xFFC2CAAB)
val FallbackDarkOnSecondary = Color(0xFF2C331C)
val FallbackDarkSecondaryContainer = Color(0xFF424931)
val FallbackDarkOnSecondaryContainer = Color(0xFFDEE6C5)

val FallbackDarkTertiary = Color(0xFFA0D0CB)
val FallbackDarkOnTertiary = Color(0xFF003735)
val FallbackDarkTertiaryContainer = Color(0xFF1F4E4B)
val FallbackDarkOnTertiaryContainer = Color(0xFFBBECE7)

val FallbackDarkBackground = Color(0xFF12140E)
val FallbackDarkOnBackground = Color(0xFFE3E3DC)
val FallbackDarkSurface = Color(0xFF12140E)
val FallbackDarkOnSurface = Color(0xFFE3E3DC)
val FallbackDarkSurfaceVariant = Color(0xFF45483C)
val FallbackDarkOnSurfaceVariant = Color(0xFFC6C8B8)
val FallbackDarkOutline = Color(0xFF909282)
val FallbackDarkSurfaceContainerLowest = Color(0xFF0D0F0A)
val FallbackDarkSurfaceContainerLow = Color(0xFF161912)
val FallbackDarkSurfaceContainer = Color(0xFF1E211A)
val FallbackDarkSurfaceContainerHigh = Color(0xFF282B24)
val FallbackDarkSurfaceContainerHighest = Color(0xFF33362E)

// Legacy colors for backwards compatibility
val CyberCyan = Color(0xFF00E5FF)
val ElectricTeal = Color(0xFF0284C7)
val SlateBackgroundDark = Color(0xFF12140E)
val SlateSurfaceDark = Color(0xFF1E211A)
val SlateSurfaceVariantDark = Color(0xFF282B24)
val SlateBorderDark = Color(0xFF45483C)

val LightBackground = Color(0xFFFAFBF1)
val LightSurface = Color(0xFFFAFBF1)
val LightSurfaceVariant = Color(0xFFE2E4D4)
val LightBorder = Color(0xFFC6C8B8)

// Accent Card Colors for Notes
val NoteColorDefault = 0L // Transparent / Theme default
val NoteColorBlue = 0xFF1E3A5F
val NoteColorTeal = 0xFF134E4A
val NoteColorPurple = 0xFF3B1E54
val NoteColorAmber = 0xFF5C3D11
val NoteColorEmerald = 0xFF14532D
val NoteColorRose = 0xFF4C1D2A
val NoteColorLime = 0xFF36480B

// Status Colors & Encryption Badges
// Darker in light mode to guarantee high contrast (>7:1) and crisp readability
val VaultEncryptedColorLight = Color(0xFF233800)          // Very dark olive-forest green for text/icons in light mode
val VaultEncryptedContainerLight = Color(0xFFD4EA92)      // Solid, legible tinted container in light mode
val VaultEncryptedBorderLight = Color(0xFF3F5D00)         // Dark crisp border in light mode

val VaultEncryptedColorDark = Color(0xFFC6F135)           // Vibrant lime for dark mode
val VaultEncryptedContainerDark = Color(0xFF222D00)       // Deep olive container for dark mode
val VaultEncryptedBorderDark = Color(0xFF5D7B00)          // Subtle lime border for dark mode

val VaultLockedColor = Color(0xFFB45309)                  // Dark amber (high contrast over bright yellow)
val VaultEncryptedColor = Color(0xFF233800)               // Darker base tone for high readability
val VaultCriticalColor = Color(0xFFB91C1C)                // Deep red for readability
