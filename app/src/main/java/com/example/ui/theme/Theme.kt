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

import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = Color(0xFF00FF66), // bright minty green
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E2E21),
    onPrimaryContainer = Color(0xFF00FF66),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFF6E6E6E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF0A0E1A), // deep dark space
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF121829),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1C253D),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = Color(0xFFFF4D4D),
    onError = Color(0xFFFFFFFF)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFF0055), // neon pink
    onPrimary = Color(0xFF12001A),
    primaryContainer = Color(0xFF33001E),
    onPrimaryContainer = Color(0xFFFF0055),
    secondary = Color(0xFF00FFFF), // neon cyan
    onSecondary = Color(0xFF12001A),
    tertiary = Color(0xFFB500FF), // neon purple
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF0B001F), // neon dark violet
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1D003E),
    onSurface = Color(0xFF00FFFF),
    surfaceVariant = Color(0xFF2C004F),
    onSurfaceVariant = Color(0xFFFFFFFF),
    error = Color(0xFFFF003C),
    onError = Color(0xFFFFFFFF)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF00D28E), // emerald green
    onPrimary = Color(0xFF0B2117),
    primaryContainer = Color(0xFF0B2D20),
    onPrimaryContainer = Color(0xFF00D28E),
    secondary = Color(0xFF8CEEC9),
    onSecondary = Color(0xFF0B2117),
    tertiary = Color(0xFF4DB089),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF07120F), // dark green-tinted charcoal
    onBackground = Color(0xFFEFF5F3),
    surface = Color(0xFF0E1F1A),
    onSurface = Color(0xFF8CEEC9),
    surfaceVariant = Color(0xFF163229),
    onSurfaceVariant = Color(0xFFEFF5F3),
    error = Color(0xFFFF453A),
    onError = Color(0xFFFFFFFF)
)

private val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFF7E1D), // hot orange
    onPrimary = Color(0xFF230C00),
    primaryContainer = Color(0xFF3B1E0C),
    onPrimaryContainer = Color(0xFFFF7E1D),
    secondary = Color(0xFFFFBF00), // golden amber
    onSecondary = Color(0xFF230C00),
    tertiary = Color(0xFFFF4D4D),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF140C07), // dark warm cocoa
    onBackground = Color(0xFFFFF6F1),
    surface = Color(0xFF211510),
    onSurface = Color(0xFFFFBF00),
    surfaceVariant = Color(0xFF332018),
    onSurfaceVariant = Color(0xFFFFF6F1),
    error = Color(0xFFFF3333),
    onError = Color(0xFFFFFFFF)
)

private val MidnightColorScheme = darkColorScheme(
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

private val DiscordColorScheme = darkColorScheme(
    primary = Color(0xFF5865F2), // Discord Blurple
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B449B),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF248046), // Discord green
    onSecondary = Color.White,
    tertiary = Color(0xFF4F545C),
    onTertiary = Color.White,
    background = Color(0xFF313338), // Discord greybg
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF2B2D31),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF1E1F22),
    onSurfaceVariant = Color(0xFFB5BAC1),
    error = Color(0xFFF23F43),
    onError = Color.White
)

private val SiyahBeyazColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF222222),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF888888),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFF444444),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF000000), // Pure Black background
    onBackground = Color(0xFFFFFFFF), // Pure White text
    surface = Color(0xFF111111),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF222222),
    onSurfaceVariant = Color(0xFFFFFFFF),
    error = Color(0xFFFFFFFF),
    onError = Color(0xFF000000)
)

private val BeyazColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF), // clean vivid royal blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF0066FF),
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    tertiary = Color(0xFFE0E0E0),
    onTertiary = Color(0xFF202020),
    background = Color(0xFFFFFFFF), // pure white background
    onBackground = Color(0xFF1F1F1F), // dark text
    surface = Color(0xFFF8F9FA),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFEDF0F2),
    onSurfaceVariant = Color(0xFF202124),
    error = Color(0xFFD93025),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    theme: String = "cosmic",
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        "cyberpunk" -> CyberpunkColorScheme
        "emerald" -> EmeraldColorScheme
        "sunset" -> SunsetColorScheme
        "midnight" -> MidnightColorScheme
        "discord" -> DiscordColorScheme
        "siyahbeyaz" -> SiyahBeyazColorScheme
        "beyaz" -> BeyazColorScheme
        else -> CosmicColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
