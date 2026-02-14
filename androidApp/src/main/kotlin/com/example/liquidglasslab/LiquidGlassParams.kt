package com.example.liquidglasslab

import androidx.compose.ui.graphics.Color

/**
 * Parameters for the Liquid Glass effect, tuned to approximate iOS `.glassEffect(.regular)`.
 */
data class LiquidGlassParams(
    // Phase 1: base params
    val refraction: Float = 0.25f,
    val curve: Float = 0.20f,
    val dispersion: Float = 0.15f,
    val saturation: Float = 1.15f,
    val contrast: Float = 1.05f,
    val tintAlpha: Float = 0.08f,
    val edge: Float = 0.25f,
    // Phase 2: specular highlights
    val specularAmplitude: Float = 1.5f,
    val specularExponent: Float = 5.0f,
    val enableSpecular: Boolean = true,
    // Phase 2: enhanced fresnel
    val fresnelWidth: Float = 6.0f,
    val fresnelBlur: Float = 5.5f,
    val enableFresnel: Boolean = true,
    // Phase 2: noise texture
    val noiseIntensity: Float = 0.02f,
    val enableNoise: Boolean = true,
    // Phase 3: gyroscope
    val enableGyroscope: Boolean = true,
) {
    val tint: Color get() = Color.White.copy(alpha = tintAlpha)

    companion object {
        val Default = LiquidGlassParams()
    }
}
