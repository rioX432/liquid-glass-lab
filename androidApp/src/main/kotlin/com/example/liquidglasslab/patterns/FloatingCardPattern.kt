package com.example.liquidglasslab.patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.LiquidGlassParams
import com.example.liquidglasslab.SampleData
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
import com.example.liquidglasslab.shader.enhancedLiquidGlass
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.foundation.background

@Composable
fun FloatingCardPattern(
    blurMode: BlurMode,
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
    liquidGlassParams: LiquidGlassParams = LiquidGlassParams.Default,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
) {
    when (blurMode) {
        BlurMode.Haze -> HazeFloatingCard(blurRadiusDp, modifier)
        BlurMode.Cloudy -> CloudyFloatingCard(blurRadiusDp, modifier)
        BlurMode.LiquidGlass -> LiquidGlassFloatingCard(blurRadiusDp, liquidGlassParams, lightDirection, modifier)
    }
}

@Composable
private fun HazeFloatingCard(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val style = HazeStyle(
        blurRadius = blurRadiusDp.dp,
        tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))),
    )

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentScale = ContentScale.Crop,
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .hazeEffect(state = hazeState, style = style),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            FloatingCardContent()
        }
    }
}

@Composable
private fun CloudyFloatingCard(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val cloudyRadius = hazeEquivalentCloudyRadius(blurRadiusDp)

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(modifier = Modifier.clipToBounds()) {
                // Blurred background layer
                AsyncImage(
                    model = SampleData.heroImage,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .cloudy(radius = cloudyRadius),
                    contentScale = ContentScale.Crop,
                )
                // Tint overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                )
                // Clear content on top
                FloatingCardContent()
            }
        }
    }
}

@Composable
private fun LiquidGlassFloatingCard(
    blurRadiusDp: Float,
    params: LiquidGlassParams,
    lightDirection: Offset,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val hazeStyle = HazeStyle(
        blurRadius = blurRadiusDp.dp,
        tints = listOf(HazeTint(params.tint)),
    )

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentScale = ContentScale.Crop,
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clipToBounds(),
            ) {
                // Haze backdrop blur layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .hazeEffect(state = hazeState, style = hazeStyle),
                )
                // LiquidGlass refraction layer
                LiquidGlassCardOverlay(
                    params = params,
                    cornerRadius = 24f,
                    lightDirection = lightDirection,
                    modifier = Modifier.matchParentSize(),
                )
                // Content on top (determines card height)
                FloatingCardContent()
            }
        }
    }
}

/** Transparent overlay that applies liquidGlass refraction for card shapes. */
@Composable
private fun LiquidGlassCardOverlay(
    params: LiquidGlassParams,
    cornerRadius: Float,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { s ->
                size = Size(s.width.toFloat(), s.height.toFloat())
            }
            .then(
                if (size.width > 0f && size.height > 0f) {
                    Modifier.enhancedLiquidGlass(
                        lensCenter = Offset(size.width / 2f, size.height / 2f),
                        lensSize = size,
                        cornerRadius = cornerRadius,
                        params = params,
                        lightDirection = lightDirection,
                    )
                } else Modifier
            ),
    )
}

@Composable
private fun FloatingCardContent() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Floating Card",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "This card floats over a background image with a blur effect applied.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "The content behind the card is blurred, creating a frosted glass appearance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
