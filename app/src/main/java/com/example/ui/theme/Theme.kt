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
    primary = AthleteLime,
    secondary = TealCardio,
    tertiary = CoralBurn,
    background = CarbonDark,
    surface = CarbonSurface,
    onPrimary = CarbonDark,
    onSecondary = CarbonDark,
    onTertiary = CarbonDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = CarbonSurfaceElevation,
    onSurfaceVariant = SoftGrayText
  )

private val LightColorScheme =
  darkColorScheme( // Keep light theme also dark and athletic since high intensity athletes prefer deep dark dashboards!
    primary = AthleteLime,
    secondary = TealCardio,
    tertiary = CoralBurn,
    background = CarbonDark,
    surface = CarbonSurface,
    onPrimary = CarbonDark,
    onSecondary = CarbonDark,
    onTertiary = CarbonDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = CarbonSurfaceElevation,
    onSurfaceVariant = SoftGrayText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
