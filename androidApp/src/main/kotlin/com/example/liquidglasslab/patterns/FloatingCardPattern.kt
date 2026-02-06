package com.example.liquidglasslab.patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.SampleData
import com.skydoves.cloudy.cloudy
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun FloatingCardPattern(
    libraryType: LibraryType,
    modifier: Modifier = Modifier,
) {
    when (libraryType) {
        LibraryType.Haze -> HazeFloatingCard(modifier)
        LibraryType.Cloudy -> CloudyFloatingCard(modifier)
    }
}

@Composable
private fun HazeFloatingCard(modifier: Modifier = Modifier) {
    val hazeState = rememberHazeState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background image
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentScale = ContentScale.Crop,
        )

        // Floating card with blur
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(24.dp)
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.regular(),
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            FloatingCardContent(libraryLabel = "Haze")
        }
    }
}

@Composable
private fun CloudyFloatingCard(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background image
        AsyncImage(
            model = SampleData.heroImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Floating card with cloudy blur
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(24.dp)
                .cloudy(radius = hazeEquivalentCloudyRadius()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ),
        ) {
            FloatingCardContent(libraryLabel = "Cloudy")
        }
    }
}

@Composable
private fun FloatingCardContent(libraryLabel: String) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Floating Card",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "This card floats over a background image with a blur effect applied using $libraryLabel.",
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
