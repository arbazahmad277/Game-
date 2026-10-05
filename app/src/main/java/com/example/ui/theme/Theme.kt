package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DesertStormColorScheme = darkColorScheme(
  primary = DesertGold,
  onPrimary = CarbonBlack,
  primaryContainer = DesertGoldDark,
  onPrimaryContainer = DesertGoldLight,
  secondary = NitroBlue,
  onSecondary = CarbonBlack,
  secondaryContainer = NitroBlueDark,
  onSecondaryContainer = NitroBlueGlow,
  tertiary = RadarGreen,
  onTertiary = CarbonBlack,
  tertiaryContainer = RadarGreenDim,
  onTertiaryContainer = RadarGreenGlow,
  error = AlertRed,
  onError = Color.White,
  errorContainer = Color(0xFF7F1D1D),
  onErrorContainer = AlertRedGlow,
  background = CarbonBlack,
  onBackground = CarbonTextPrimary,
  surface = CarbonDark,
  onSurface = CarbonTextPrimary,
  surfaceVariant = CarbonSurface,
  onSurfaceVariant = CarbonTextSecondary,
  outline = CarbonBorder,
  outlineVariant = CarbonBorder.copy(alpha = 0.5f)
)

@Composable
fun DesertStormTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DesertStormColorScheme,
    typography = Typography,
    content = content
  )
}
