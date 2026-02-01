package com.example.liquidglasslab.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.SampleData
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass

@Composable
fun CloudyScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Blur", "Liquid Glass", "Combined")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> CloudyBlurTab()
            1 -> LiquidGlassTab()
            2 -> CombinedTab()
        }
    }
}

@Composable
private fun CloudyBlurTab() {
    var blurRadius by remember { mutableIntStateOf(15) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Cloudy Blur", style = MaterialTheme.typography.headlineSmall)

        // Blur radius slider
        SliderRow(
            label = "Blur Radius",
            value = blurRadius.toFloat(),
            valueRange = 0f..30f,
            valueLabel = "$blurRadius",
            onValueChange = { blurRadius = it.toInt() },
        )

        // Background with blur applied
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            AsyncImage(
                model = SampleData.heroImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .cloudy(radius = blurRadius),
                contentScale = ContentScale.Crop,
            )
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Blur radius: $blurRadius",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Grid with blur
        Text("Grid with blur", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SampleData.sampleImages.take(4)) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .cloudy(radius = blurRadius),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassTab() {
    var refraction by remember { mutableFloatStateOf(0.25f) }
    var curve by remember { mutableFloatStateOf(0.25f) }
    var dispersion by remember { mutableFloatStateOf(0.0f) }
    var cornerRadius by remember { mutableFloatStateOf(50f) }

    // Drag state for lens position
    var lensCenter by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var initialized by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Liquid Glass Lens", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Drag on the image to move the lens",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Parameter sliders
        SliderRow("Refraction", refraction, 0f..1f, "%.2f".format(refraction)) { refraction = it }
        SliderRow("Curve", curve, 0f..1f, "%.2f".format(curve)) { curve = it }
        SliderRow("Dispersion", dispersion, 0f..0.5f, "%.2f".format(dispersion)) { dispersion = it }
        SliderRow("Corner Radius", cornerRadius, 0f..150f, "%.0f".format(cornerRadius)) { cornerRadius = it }

        // Image with liquid glass effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .onSizeChanged { size ->
                    containerSize = Size(size.width.toFloat(), size.height.toFloat())
                    if (!initialized) {
                        lensCenter = Offset(size.width / 2f, size.height / 2f)
                        initialized = true
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        lensCenter = Offset(
                            x = (lensCenter.x + dragAmount.x).coerceIn(0f, containerSize.width),
                            y = (lensCenter.y + dragAmount.y).coerceIn(0f, containerSize.height),
                        )
                    }
                },
        ) {
            AsyncImage(
                model = SampleData.heroImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlass(
                        lensCenter = lensCenter,
                        lensSize = Size(350f, 350f),
                        cornerRadius = cornerRadius,
                        refraction = refraction,
                        curve = curve,
                        dispersion = dispersion,
                    ),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun CombinedTab() {
    var blurRadius by remember { mutableIntStateOf(10) }
    var refraction by remember { mutableFloatStateOf(0.25f) }
    var lensCenter by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var initialized by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Cloudy + Liquid Glass", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Both effects combined",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SliderRow("Blur", blurRadius.toFloat(), 0f..25f, "$blurRadius") { blurRadius = it.toInt() }
        SliderRow("Refraction", refraction, 0f..1f, "%.2f".format(refraction)) { refraction = it }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .onSizeChanged { size ->
                    containerSize = Size(size.width.toFloat(), size.height.toFloat())
                    if (!initialized) {
                        lensCenter = Offset(size.width / 2f, size.height / 2f)
                        initialized = true
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        lensCenter = Offset(
                            x = (lensCenter.x + dragAmount.x).coerceIn(0f, containerSize.width),
                            y = (lensCenter.y + dragAmount.y).coerceIn(0f, containerSize.height),
                        )
                    }
                },
        ) {
            AsyncImage(
                model = SampleData.heroImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .cloudy(radius = blurRadius)
                    .liquidGlass(
                        lensCenter = lensCenter,
                        lensSize = Size(300f, 300f),
                        refraction = refraction,
                    ),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueLabel, style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}
