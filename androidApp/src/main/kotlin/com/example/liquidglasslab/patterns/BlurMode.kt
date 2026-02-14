package com.example.liquidglasslab.patterns

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

enum class BlurMode(val label: String) {
    Haze("Haze"),
    Cloudy("Cloudy"),
    LiquidGlass("Liquid Glass"),
}

enum class PatternType(val label: String) {
    AppBarBottomBar("NavBar + Bar"),
    FloatingCard("Card"),
    FullScreenOverlay("Overlay"),
}

/**
 * Haze の blurRadius (dp) と等価な Cloudy radius (px) を算出。
 *   Haze  sigma = blurRadiusDp × density
 *   Cloudy sigma = radiusPx / 2
 * → radiusPx = 2 × blurRadiusDp × density
 */
@Composable
fun hazeEquivalentCloudyRadius(hazeBlurRadiusDp: Float = 24f): Int {
    val density = LocalDensity.current.density
    return (2f * hazeBlurRadiusDp * density).roundToInt()
}
