package aih.iikrhia.haxe

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme = darkColorScheme(
    background = gelesaiDark,
    surface = tanekgeleDark,
    surfaceVariant = tanekDark,
    surfaceContainer = tanekDark,
    surfaceContainerHigh = tanekp2saDark,
    surfaceContainerHighest = tanekt2xaDark,
    surfaceContainerLow = tanekgeleDark,
    surfaceContainerLowest = gelesaiDark,
    onBackground = kp6Dark,
    onSurface = kp6Dark,
    onSurfaceVariant = kp6p2saDark,
    outline = sakp6Dark,
    outlineVariant = sakp6p2saDark,
    primary = kp6Dark,
    onPrimary = gelesaiDark
)

private val LightColorScheme = lightColorScheme(
    background = gelesaiLight,
    surface = tanekgeleLight,
    surfaceVariant = tanekLight,
    surfaceContainer = tanekLight,
    surfaceContainerHigh = tanekp2saLight,
    surfaceContainerHighest = tanekt2xaLight,
    surfaceContainerLow = tanekgeleLight,
    surfaceContainerLowest = gelesaiLight,
    onBackground = kp6Light,
    onSurface = kp6Light,
    onSurfaceVariant = kp6p2saLight,
    outline = sakp6Light,
    outlineVariant = sakp6p2saLight,
    primary = kp6Light,
    onPrimary = gelesaiLight
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val customTypography = androidx.compose.runtime.remember(fontFamily) {
        val features = "liga, clig, dlig, hlig, calt"
        androidx.compose.material3.Typography(
            displayLarge = Typography.displayLarge.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            displayMedium = Typography.displayMedium.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            displaySmall = Typography.displaySmall.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            headlineLarge = Typography.headlineLarge.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            headlineMedium = Typography.headlineMedium.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            headlineSmall = Typography.headlineSmall.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            titleLarge = Typography.titleLarge.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            titleMedium = Typography.titleMedium.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            titleSmall = Typography.titleSmall.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            bodyLarge = Typography.bodyLarge.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            bodyMedium = Typography.bodyMedium.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            bodySmall = Typography.bodySmall.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            labelLarge = Typography.labelLarge.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            labelMedium = Typography.labelMedium.copy(fontFamily = fontFamily, fontFeatureSettings = features),
            labelSmall = Typography.labelSmall.copy(fontFamily = fontFamily, fontFeatureSettings = features)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
