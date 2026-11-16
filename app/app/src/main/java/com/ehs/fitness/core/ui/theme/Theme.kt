package com.ehs.fitness.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Cores fora do esquema Material: estados "soft", sidebar escura, bordas e textos sutis.
 * Espelham os tokens `--color-*` do front web.
 */
@Immutable
data class FitnessColors(
    val secondary: Color,
    val secondarySoft: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val sidebar: Color,
    val sidebarText: Color,
    val subtle: Color,
    val primarySoft: Color,
    val border: Color,
    val borderStrong: Color,
)

private val LightExtended = FitnessColors(
    secondary = LightSecondary,
    secondarySoft = LightSecondarySoft,
    success = LightSuccess,
    successSoft = LightSuccessSoft,
    warning = LightWarning,
    warningSoft = LightWarningSoft,
    danger = LightDanger,
    dangerSoft = LightDangerSoft,
    sidebar = LightSidebar,
    sidebarText = LightSidebarText,
    subtle = LightSubtle,
    primarySoft = LightPrimarySoft,
    border = LightBorder,
    borderStrong = LightBorderStrong,
)

private val DarkExtended = FitnessColors(
    secondary = DarkSecondary,
    secondarySoft = DarkSecondarySoft,
    success = DarkSuccess,
    successSoft = DarkSuccessSoft,
    warning = DarkWarning,
    warningSoft = DarkWarningSoft,
    danger = DarkDanger,
    dangerSoft = DarkDangerSoft,
    sidebar = DarkSidebar,
    sidebarText = DarkSidebarText,
    subtle = DarkSubtle,
    primarySoft = DarkPrimarySoft,
    border = DarkBorder,
    borderStrong = DarkBorderStrong,
)

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimarySoft,
    onPrimaryContainer = Color(0xFF312E81),
    secondary = LightSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LightSecondarySoft,
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = LightWarning,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = LightWarningSoft,
    onTertiaryContainer = Color(0xFF7C2D12),
    error = LightDanger,
    onError = Color(0xFFFFFFFF),
    errorContainer = LightDangerSoft,
    onErrorContainer = Color(0xFF991B1B),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = LightMuted,
    outline = LightBorderStrong,
    outlineVariant = LightBorder,
    inverseSurface = LightSidebar,
    inverseOnSurface = Color(0xFFF4F4F5),
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimarySoft,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF221046),
    secondaryContainer = DarkSecondarySoft,
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = DarkWarning,
    onTertiary = Color(0xFF3B2C08),
    tertiaryContainer = DarkWarningSoft,
    onTertiaryContainer = Color(0xFFFFE7AF),
    error = DarkDanger,
    onError = Color(0xFF3B1207),
    errorContainer = DarkDangerSoft,
    onErrorContainer = Color(0xFFFFD9CE),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF27272F),
    onSurfaceVariant = DarkMuted,
    outline = DarkBorderStrong,
    outlineVariant = DarkBorder,
    inverseSurface = DarkText,
    inverseOnSurface = DarkBackground,
)

val LocalFitnessColors = staticCompositionLocalOf { LightExtended }

@Composable
fun FitnessTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkExtended else LightExtended
    CompositionLocalProvider(LocalFitnessColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = FitnessTypography,
            content = content,
        )
    }
}

object FitnessTheme {
    val colors: FitnessColors
        @Composable get() = LocalFitnessColors.current
}
