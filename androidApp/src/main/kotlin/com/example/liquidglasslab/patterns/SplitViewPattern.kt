package com.example.liquidglasslab.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.SampleData
import com.skydoves.cloudy.cloudy
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

@Composable
fun SplitViewPattern(
    modifier: Modifier = Modifier,
) {
    var splitPosition by remember { mutableFloatStateOf(0.5f) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Column(modifier = modifier.fillMaxSize()) {
        // Instructions
        Text(
            "Drag the divider to compare",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .onSizeChanged { size ->
                    containerSize = Size(size.width.toFloat(), size.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val newPosition = change.position.x / containerSize.width
                        splitPosition = newPosition.coerceIn(0.1f, 0.9f)
                    }
                },
        ) {
            // Left side: Haze
            HazeSide(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(splitPosition)
                    .clipToBounds(),
            )

            // Right side: Cloudy
            CloudySide(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f - splitPosition)
                    .align(Alignment.CenterEnd)
                    .clipToBounds(),
            )

            // Divider line
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (containerSize.width * splitPosition).roundToInt() - with(density) { 2.dp.roundToPx() },
                            y = 0,
                        )
                    }
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color.White),
            )

            // Labels
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            ) {
                Text(
                    "Haze",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Box(modifier = Modifier.weight(1f))
                Text(
                    "Cloudy",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            // Split percentage indicator
            Text(
                "${(splitPosition * 100).toInt()}% / ${((1 - splitPosition) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun HazeSide(modifier: Modifier = Modifier) {
    val hazeState = rememberHazeState()

    Box(modifier = modifier) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentScale = ContentScale.Crop,
        )

        // Blur overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .hazeEffect(state = hazeState, style = HazeMaterials.regular()),
        )
    }
}

@Composable
private fun CloudySide(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Blur overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .cloudy(radius = 20),
        )
    }
}
