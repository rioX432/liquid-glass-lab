package com.example.liquidglasslab.patterns

import androidx.compose.foundation.background
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
import com.example.liquidglasslab.SampleData
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun FloatingCardPattern(
    blurMode: BlurMode,
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    when (blurMode) {
        BlurMode.Haze -> HazeFloatingCard(blurRadiusDp, modifier)
        BlurMode.Cloudy -> CloudyFloatingCard(blurRadiusDp, modifier)
        BlurMode.LiquidGlass -> LiquidGlassFloatingCard(blurRadiusDp, modifier)
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
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clipToBounds(),
            ) {
                // Blurred + refracted background
                LiquidGlassCardBackground(
                    imageUrl = SampleData.heroImage,
                    cloudyRadius = cloudyRadius,
                    cornerRadius = 24f,
                    modifier = Modifier.matchParentSize(),
                )
                // Tint overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                // Content on top (determines card height)
                FloatingCardContent()
            }
        }
    }
}

/** Blurred + refracted background for card-shaped liquid glass. */
@Composable
private fun LiquidGlassCardBackground(
    imageUrl: String,
    cloudyRadius: Int,
    cornerRadius: Float,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(Size.Zero) }

    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = modifier
            .onSizeChanged { s ->
                size = Size(s.width.toFloat(), s.height.toFloat())
            }
            .cloudy(radius = cloudyRadius)
            .then(
                if (size.width > 0f && size.height > 0f) {
                    Modifier.liquidGlass(
                        lensCenter = Offset(size.width / 2f, size.height / 2f),
                        lensSize = size,
                        cornerRadius = cornerRadius,
                        refraction = 0.2f,
                        curve = 0.15f,
                    )
                } else Modifier
            ),
        contentScale = ContentScale.Crop,
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
