package com.example.liquidglasslab.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clipToBounds
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

@Composable
fun FullScreenOverlayPattern(
    blurMode: BlurMode,
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
    liquidGlassParams: LiquidGlassParams = LiquidGlassParams.Default,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
) {
    when (blurMode) {
        BlurMode.Haze -> HazeFullScreenOverlay(blurRadiusDp, modifier)
        BlurMode.Cloudy -> CloudyFullScreenOverlay(blurRadiusDp, modifier)
        BlurMode.LiquidGlass -> LiquidGlassFullScreenOverlay(blurRadiusDp, liquidGlassParams, lightDirection, modifier)
    }
}

@Composable
private fun HazeFullScreenOverlay(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val style = HazeStyle(
        blurRadius = blurRadiusDp.dp,
        tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))),
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(state = hazeState, style = style),
            contentAlignment = Alignment.Center,
        ) {
            ModalContent()
        }
    }
}

@Composable
private fun CloudyFullScreenOverlay(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val cloudyRadius = hazeEquivalentCloudyRadius(blurRadiusDp)

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .cloudy(radius = cloudyRadius),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            contentAlignment = Alignment.Center,
        ) {
            ModalContent()
        }
    }
}

@Composable
private fun LiquidGlassFullScreenOverlay(
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
    var containerSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentScale = ContentScale.Crop,
        )

        // Haze backdrop blur layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(state = hazeState, style = hazeStyle),
        )

        // LiquidGlass refraction layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    containerSize = Size(size.width.toFloat(), size.height.toFloat())
                }
                .then(
                    if (containerSize.width > 0f && containerSize.height > 0f) {
                        Modifier.enhancedLiquidGlass(
                            lensCenter = Offset(containerSize.width / 2f, containerSize.height / 2f),
                            lensSize = containerSize,
                            cornerRadius = 0f,
                            params = params,
                            lightDirection = lightDirection,
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            ModalContent()
        }
    }
}

@Composable
private fun ModalContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Confirm Action",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "This modal demonstrates a full-screen blur overlay. The background content is blurred behind this dialog.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm")
                }
            }
        }
    }
}
