package com.example.liquidglasslab.shader

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.liquidglasslab.LiquidGlassParams

/**
 * Enhanced Liquid Glass modifier using a custom AGSL shader with
 * specular highlights, noise texture, and enhanced Fresnel.
 *
 * Requires API 33+. On older APIs, this is a no-op.
 */
@Composable
fun Modifier.enhancedLiquidGlass(
    lensCenter: Offset,
    lensSize: Size,
    cornerRadius: Float = 0f,
    params: LiquidGlassParams = LiquidGlassParams.Default,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
    enabled: Boolean = true,
): Modifier {
    if (!enabled || lensSize.width <= 0f || lensSize.height <= 0f) {
        return this
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return this
    }

    return enhancedLiquidGlassApi33(
        lensCenter = lensCenter,
        lensSize = lensSize,
        cornerRadius = cornerRadius,
        params = params,
        lightDirection = lightDirection,
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.enhancedLiquidGlassApi33(
    lensCenter: Offset,
    lensSize: Size,
    cornerRadius: Float,
    params: LiquidGlassParams,
    lightDirection: Offset,
): Modifier {
    val shader = remember {
        try {
            RuntimeShader(EnhancedLiquidGlassShaderSource.AGSL)
        } catch (e: Exception) {
            Log.w("EnhancedLiquidGlass", "RuntimeShader compilation failed", e)
            null
        }
    }

    if (shader == null) {
        return this
    }

    return this.graphicsLayer {
        val width = size.width
        val height = size.height

        if (width > 0 && height > 0) {
            shader.setFloatUniform("resolution", width, height)
            shader.setFloatUniform("lensCenter", lensCenter.x, lensCenter.y)
            shader.setFloatUniform("lensSize", lensSize.width, lensSize.height)
            shader.setFloatUniform("cornerRadius", cornerRadius)

            // Base params (same as Cloudy)
            shader.setFloatUniform("refraction", params.refraction)
            shader.setFloatUniform("curve", params.curve)
            shader.setFloatUniform("dispersion", params.dispersion)
            shader.setFloatUniform("saturation", params.saturation)
            shader.setFloatUniform("contrast", params.contrast)
            shader.setFloatUniform("tint", params.tint.red, params.tint.green, params.tint.blue, params.tint.alpha)
            shader.setFloatUniform("edge", params.edge)

            // Phase 2: specular
            shader.setFloatUniform("specularAmplitude", params.specularAmplitude)
            shader.setFloatUniform("specularExponent", params.specularExponent)
            shader.setFloatUniform("enableSpecular", if (params.enableSpecular) 1f else 0f)

            // Phase 2: fresnel
            shader.setFloatUniform("fresnelWidth", params.fresnelWidth)
            shader.setFloatUniform("fresnelBlur", params.fresnelBlur)
            shader.setFloatUniform("enableFresnel", if (params.enableFresnel) 1f else 0f)

            // Phase 2: noise
            shader.setFloatUniform("noiseIntensity", params.noiseIntensity)
            shader.setFloatUniform("enableNoise", if (params.enableNoise) 1f else 0f)

            // Light direction (Phase 3 will make this dynamic via gyroscope)
            shader.setFloatUniform("lightDirection", lightDirection.x, lightDirection.y)

            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()
        }
    }
}
