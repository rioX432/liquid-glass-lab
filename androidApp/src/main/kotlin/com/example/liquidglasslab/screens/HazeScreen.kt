package com.example.liquidglasslab.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.SampleData
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazeScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Presets", "Custom", "Scroll-aware")

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
            0 -> HazePresetsTab()
            1 -> HazeCustomTab()
            2 -> HazeScrollAwareTab()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HazePresetsTab() {
    val hazeState = rememberHazeState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SampleData.sampleImages) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        TopAppBar(
            title = { Text("Haze Presets") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HazeMaterialCard("Ultra Thin", hazeState, HazeMaterials.ultraThin())
            HazeMaterialCard("Thin", hazeState, HazeMaterials.thin())
            HazeMaterialCard("Regular", hazeState, HazeMaterials.regular())
            ProgressiveBlurCard(hazeState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HazeCustomTab() {
    val hazeState = rememberHazeState()

    var blurRadius by remember { mutableFloatStateOf(20f) }
    var noise by remember { mutableFloatStateOf(0.1f) }
    var tintAlpha by remember { mutableFloatStateOf(0.3f) }

    val tintColor = MaterialTheme.colorScheme.surface.copy(alpha = tintAlpha)

    val customStyle = HazeStyle(
        blurRadius = blurRadius.dp,
        noiseFactor = noise,
        tints = listOf(HazeTint(tintColor)),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 200.dp),
        ) {
            // Controls
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Custom Haze Controls", style = MaterialTheme.typography.headlineSmall)

                SliderRow("Blur Radius", blurRadius, 0f..50f, "%.0f dp".format(blurRadius)) {
                    blurRadius = it
                }
                SliderRow("Noise", noise, 0f..0.5f, "%.2f".format(noise)) { noise = it }
                SliderRow("Tint Alpha", tintAlpha, 0f..1f, "%.2f".format(tintAlpha)) { tintAlpha = it }
            }

            // Image grid as blur source
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                items(SampleData.sampleImages.take(6)) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        // Floating card with custom haze effect
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .hazeEffect(state = hazeState, style = customStyle),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Custom Style Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "blur: ${blurRadius.toInt()}dp | noise: ${"%.2f".format(noise)} | tint: ${"%.0f".format(tintAlpha * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HazeScrollAwareTab() {
    val hazeState = rememberHazeState()
    val gridState = rememberLazyGridState()

    // Derive alpha from scroll position
    val scrollProgress by remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val offset = gridState.firstVisibleItemScrollOffset.toFloat()
                (offset / 200f).coerceIn(0f, 1f)
            }
        }
    }

    // Animate alpha changes
    val animatedAlpha by animateFloatAsState(targetValue = scrollProgress, label = "hazeAlpha")

    // Dynamic blur radius based on scroll
    val dynamicBlurRadius: Dp = (animatedAlpha * 25f).dp

    val dynamicStyle = HazeStyle(
        blurRadius = dynamicBlurRadius,
        noiseFactor = 0.1f * animatedAlpha,
        tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f * animatedAlpha))),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(top = 72.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SampleData.sampleImages + SampleData.sampleImages) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        TopAppBar(
            title = {
                Column {
                    Text("Scroll-aware Blur")
                    Text(
                        "Alpha: ${"%.0f".format(animatedAlpha * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.hazeEffect(state = hazeState, style = dynamicStyle),
        )
    }
}

@Composable
private fun HazeMaterialCard(
    label: String,
    hazeState: dev.chrisbanes.haze.HazeState,
    style: dev.chrisbanes.haze.HazeStyle,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = style),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProgressiveBlurCard(hazeState: dev.chrisbanes.haze.HazeState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .hazeEffect(state = hazeState) {
                progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column {
                Text(
                    "Progressive Blur",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Gradient: strong → transparent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}
