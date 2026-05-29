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

private val DarkColorScheme =
  darkColorScheme(
    primary = PureWhite,
    onPrimary = Black,
    primaryContainer = MediumGrey,
    onPrimaryContainer = PureWhite,
    secondary = OffWhite,
    onSecondary = Black,
    tertiary = LightGrey,
    onTertiary = PureWhite,
    background = Black,
    onBackground = PureWhite,
    surface = DarkGrey,
    onSurface = PureWhite,
    surfaceVariant = MediumGrey,
    onSurfaceVariant = OffWhite,
    error = RedWarning,
    onError = PureWhite
  )

private val LightColorScheme = DarkColorScheme // Keep same black/white dark look on all modes for Rave Co vibe

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode for premium video experience
  dynamicColor: Boolean = false, // Disable to preserve signature black & white theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
