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
    primary = GeoDarkPrimary,
    secondary = GeoDarkSecondary,
    tertiary = GeoDarkTertiary,
    background = GeoDarkBackground,
    surface = GeoDarkSurface,
    surfaceVariant = GeoDarkSurfaceVariant,
    onPrimary = GeoDarkBackground,
    onSecondary = GeoDarkBackground,
    onBackground = GeoDarkOnBackground,
    onSurface = GeoDarkOnSurface,
    onSurfaceVariant = GeoDarkMutedText,
    outline = GeoDarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GeoLightPrimary,
    secondary = GeoLightSecondary,
    tertiary = GeoLightTertiary,
    background = GeoLightBackground,
    surface = GeoLightSurface,
    surfaceVariant = GeoLightSurfaceVariant,
    onPrimary = GeoLightSurface,
    onSecondary = GeoLightOnSurface,
    onBackground = GeoLightOnBackground,
    onSurface = GeoLightOnSurface,
    onSurfaceVariant = GeoLightMutedText,
    outline = GeoLightBorder,
    outlineVariant = GeoLightBorderSubtle
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to retain the gorgeous custom Saffron & Indigo branding
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
