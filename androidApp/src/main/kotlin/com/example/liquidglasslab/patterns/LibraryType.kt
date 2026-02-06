package com.example.liquidglasslab.patterns

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

enum class LibraryType(val label: String) {
    Haze("Haze"),
    Cloudy("Cloudy"),
}

enum class PatternType(val label: String) {
    AppBarBottomBar("AppBar + BottomBar"),
    FloatingCard("Floating Card"),
    FullScreenOverlay("Full Screen Overlay"),
    ABToggle("A/B Toggle"),
    SplitView("Split View"),
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
    return (2f * hazeBlurRadiusDp * density).toInt()
}
