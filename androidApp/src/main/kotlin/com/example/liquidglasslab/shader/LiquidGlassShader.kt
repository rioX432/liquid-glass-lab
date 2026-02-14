package com.example.liquidglasslab.shader

/**
 * Enhanced AGSL shader for Liquid Glass effect.
 *
 * Based on Cloudy's LiquidGlassShaderSource (Apache 2.0, skydoves/Jaewoong Eum).
 * Extended with:
 * - Specular highlights (diagonal gradient + pow exponent)
 * - Noise texture (hash-based pseudo-random)
 * - Enhanced Fresnel rim lighting (Schlick approximation, configurable light direction)
 *
 * Original: https://github.com/skydoves/cloudy
 * License: Apache License 2.0
 */
object EnhancedLiquidGlassShaderSource {

    const val AGSL: String = """
uniform float2 resolution;
uniform float2 lensCenter;
uniform float2 lensSize;
uniform float cornerRadius;
uniform float refraction;
uniform float curve;
uniform float dispersion;
uniform float saturation;
uniform float contrast;
uniform float4 tint;
uniform float edge;

// Phase 2 additions
uniform float specularAmplitude;   // default 1.5
uniform float specularExponent;    // default 5.0
uniform float2 lightDirection;     // normalized, default (-0.707, -0.707)
uniform float fresnelWidth;        // default 6.0
uniform float fresnelBlur;         // default 5.5
uniform float noiseIntensity;      // default 0.02

// Enable flags (0.0 = off, 1.0 = on)
uniform float enableSpecular;
uniform float enableNoise;
uniform float enableFresnel;

uniform shader content;

const float ANTIALIAS_RADIUS = 1.5;

// SDF: rounded rectangle distance (negative inside, positive outside)
float roundedRectDistance(float2 point, float2 boxExtent, float radius) {
    float2 offsetFromCorner = abs(point) - boxExtent + float2(radius);
    float outsideDistance = length(max(offsetFromCorner, 0.0));
    float insideDistance = min(max(offsetFromCorner.x, offsetFromCorner.y), 0.0);
    return outsideDistance + insideDistance - radius;
}

// Surface gradient (outward-pointing normal at a point)
float2 calculateSurfaceGradient(float2 point, float2 boxExtent, float radius) {
    float2 innerOffset = abs(point) - boxExtent + float2(radius);
    float2 signVector = float2(
        point.x >= 0.0 ? 1.0 : -1.0,
        point.y >= 0.0 ? 1.0 : -1.0
    );

    if (max(innerOffset.x, innerOffset.y) > 0.0) {
        float2 clampedOffset = max(innerOffset, 0.0);
        return signVector * normalize(clampedOffset);
    }

    if (innerOffset.x > innerOffset.y) {
        return float2(signVector.x, 0.0);
    }
    return float2(0.0, signVector.y);
}

// Rec. 709 luminance
float getLuminance(half3 rgb) {
    return dot(rgb, half3(0.2126, 0.7152, 0.0722));
}

// Color grading: saturation, contrast, tint
half3 applyColorGrading(half3 inputColor, float satLevel, float contrastLevel, float4 tintOverlay) {
    float gray = getLuminance(inputColor);
    half3 saturatedColor = half3(clamp(mix(half3(gray), inputColor, satLevel), 0.0, 1.0));
    half3 contrastedColor = half3(clamp((saturatedColor - 0.5) * contrastLevel + 0.5, 0.0, 1.0));
    return mix(contrastedColor, half3(tintOverlay.rgb), tintOverlay.a);
}

// Hash-based pseudo-random noise (no texture sampler needed in AGSL)
float hash(float2 p) {
    float3 p3 = fract(float3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

half4 main(float2 fragCoord) {
    float2 halfExtent = lensSize * 0.5;
    float clampedRadius = min(cornerRadius, min(halfExtent.x, halfExtent.y));

    float2 localPos = fragCoord - lensCenter;
    float dist = roundedRectDistance(localPos, halfExtent, clampedRadius);

    // Early exit outside lens
    if (dist > ANTIALIAS_RADIUS) {
        return content.eval(fragCoord);
    }

    float2 surfaceDir = calculateSurfaceGradient(localPos, halfExtent, clampedRadius);

    // --- Refraction ---
    float2 samplingCoord = fragCoord;
    if (refraction > 0.0 && curve > 0.0) {
        float minExtent = min(halfExtent.x, halfExtent.y);
        float normalizedDepth = clamp(-dist / (minExtent * refraction), 0.0, 1.0);
        float sphericalFactor = 1.0 - normalizedDepth;
        float bendAmount = 1.0 - sqrt(1.0 - sphericalFactor * sphericalFactor);
        float displacement = bendAmount * curve * minExtent;
        samplingCoord = fragCoord - displacement * surfaceDir;
    }

    // --- Chromatic dispersion ---
    half4 sampledColor;
    if (dispersion > 0.0) {
        float2 normalizedPos = localPos / halfExtent;
        float2 chromaticShift = dispersion * normalizedPos * normalizedPos * normalizedPos * min(halfExtent.x, halfExtent.y) * 0.1;

        float2 redCoord = samplingCoord - chromaticShift;
        float2 greenCoord = samplingCoord;
        float2 blueCoord = samplingCoord + chromaticShift;

        float redDist = roundedRectDistance(redCoord - lensCenter, halfExtent, clampedRadius);
        float blueDist = roundedRectDistance(blueCoord - lensCenter, halfExtent, clampedRadius);

        half4 greenSample = content.eval(greenCoord);
        half4 redSample = (redDist <= 0.0) ? content.eval(redCoord) : greenSample;
        half4 blueSample = (blueDist <= 0.0) ? content.eval(blueCoord) : greenSample;

        sampledColor = half4(redSample.r, greenSample.g, blueSample.b, greenSample.a);
    } else {
        sampledColor = content.eval(samplingCoord);
    }

    if (sampledColor.a <= 0.0) {
        sampledColor = content.eval(fragCoord);
    }

    // --- Color grading ---
    sampledColor.rgb = applyColorGrading(sampledColor.rgb, saturation, contrast, tint);

    // --- Enhanced Fresnel rim lighting (Schlick approximation) ---
    if (enableFresnel > 0.5 && fresnelWidth > 0.0) {
        // NdotL: how much the surface normal aligns with light direction
        float NdotL = max(dot(surfaceDir, normalize(lightDirection)), 0.0);
        // Schlick fresnel: stronger at glancing angles
        float fresnel = pow(1.0 - NdotL, 3.0);
        // Spatial mask: only near edges (controlled by fresnelWidth and fresnelBlur)
        float edgeMask = smoothstep(-fresnelWidth, -fresnelBlur, dist);
        sampledColor.rgb += half3(fresnel * edgeMask * edge);
    } else if (edge > 0.0) {
        // Fallback: original simple rim lighting
        float rimFactor = smoothstep(-edge * 10.0, 0.0, dist);
        float2 lightDir = normalize(float2(-1.0, -1.0));
        float lightIntensity = abs(dot(surfaceDir, lightDir));
        sampledColor.rgb += half3(rimFactor * lightIntensity * edge);
    }

    // --- Specular highlights (iOS Layer 8: diagonal gradient) ---
    if (enableSpecular > 0.5 && specularAmplitude > 0.0) {
        // Normalized position within lens [-1, 1]
        float2 uv = localPos / halfExtent;
        // Diagonal gradient along light direction
        float diag = dot(uv, normalize(lightDirection));
        // Remap to [0, 1] and apply power for sharp peak
        float specBase = clamp(diag * 0.5 + 0.5, 0.0, 1.0);
        float specular = pow(specBase, specularExponent) * specularAmplitude;
        // Mask to lens interior (fade near edges)
        float interiorMask = clamp(-dist / max(min(halfExtent.x, halfExtent.y) * 0.1, 1.0), 0.0, 1.0);
        sampledColor.rgb += half3(specular * interiorMask * 0.15);
    }

    // --- Noise texture (iOS Layer 10: subtle surface texture) ---
    if (enableNoise > 0.5 && noiseIntensity > 0.0) {
        float n = hash(fragCoord) * 2.0 - 1.0;  // [-1, 1]
        sampledColor.rgb += half3(n * noiseIntensity);
    }

    // --- Anti-aliased edge blend ---
    float edgeAlpha = 1.0 - smoothstep(-ANTIALIAS_RADIUS * 0.5, ANTIALIAS_RADIUS * 0.5, dist);

    half4 originalColor = content.eval(fragCoord);
    return mix(originalColor, sampledColor, edgeAlpha);
}
"""
}
