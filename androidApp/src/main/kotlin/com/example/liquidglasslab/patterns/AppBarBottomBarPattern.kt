package com.example.liquidglasslab.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun AppBarBottomBarPattern(
    libraryType: LibraryType,
    modifier: Modifier = Modifier,
) {
    when (libraryType) {
        LibraryType.Haze -> HazeAppBarBottomBar(modifier)
        LibraryType.Cloudy -> CloudyAppBarBottomBar(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HazeAppBarBottomBar(modifier: Modifier = Modifier) {
    val hazeState = rememberHazeState()

    Box(modifier = modifier.fillMaxSize()) {
        // Content: image grid as blur source
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 72.dp,
                bottom = 88.dp,
            ),
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

        // Top bar with blur
        TopAppBar(
            title = { Text("Haze Gallery") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            modifier = Modifier
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin(),
                ),
        )

        // Bottom bar with blur
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin(),
                ),
            containerColor = Color.Transparent,
        ) {
            BottomBarContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudyAppBarBottomBar(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Content: image grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 72.dp,
                bottom = 88.dp,
            ),
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

        // Top bar with cloudy blur
        TopAppBar(
            title = { Text("Cloudy Gallery") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            ),
            modifier = Modifier.cloudy(radius = hazeEquivalentCloudyRadius()),
        )

        // Bottom bar with cloudy blur
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .cloudy(radius = hazeEquivalentCloudyRadius()),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        ) {
            BottomBarContent()
        }
    }
}

@Composable
private fun RowScope.BottomBarContent() {
    val icons = listOf(
        Icons.Default.Home to "Home",
        Icons.Default.Search to "Search",
        Icons.Default.Favorite to "Favorites",
        Icons.Default.Person to "Profile",
    )
    icons.forEach { (icon, description) ->
        IconButton(
            onClick = { },
            modifier = Modifier.weight(1f),
        ) {
            Icon(icon, contentDescription = description)
        }
    }
}
