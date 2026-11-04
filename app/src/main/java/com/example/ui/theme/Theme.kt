package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = PartyPink,
  onPrimary = TextPrimaryLight,
  primaryContainer = PartyPurpleDark,
  onPrimaryContainer = PartyPinkLight,
  secondary = PartyCyan,
  onSecondary = TextPrimaryDark,
  secondaryContainer = PartyPurple,
  onSecondaryContainer = PartyYellowLight,
  tertiary = PartyYellow,
  onTertiary = TextPrimaryDark,
  background = NightBg,
  onBackground = TextPrimaryLight,
  surface = NightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = NightCard,
  onSurfaceVariant = TextSecondaryLight
)

private val LightColorScheme = lightColorScheme(
  primary = PartyPurple,
  onPrimary = TextPrimaryLight,
  primaryContainer = PartyPurpleLight,
  onPrimaryContainer = PartyPurpleDark,
  secondary = PartyPink,
  onSecondary = TextPrimaryLight,
  secondaryContainer = PartyPinkLight,
  onSecondaryContainer = PartyPink,
  tertiary = PartyYellow,
  onTertiary = TextPrimaryDark,
  background = LightBg,
  onBackground = TextPrimaryDark,
  surface = LightSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = PartyPurpleLight,
  onSurfaceVariant = TextSecondaryDark
)

@Composable
fun PartyMapTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      window.statusBarColor = colorScheme.background.toArgb()
      window.navigationBarColor = colorScheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
