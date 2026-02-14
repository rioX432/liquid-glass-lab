package com.example.liquidglasslab.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.liquidglasslab.LiquidGlassParams
import com.example.liquidglasslab.SampleData
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
import com.example.liquidglasslab.shader.enhancedLiquidGlass
import androidx.compose.foundation.background

@Composable
fun AppBarBottomBarPattern(
    blurMode: BlurMode,
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
    liquidGlassParams: LiquidGlassParams = LiquidGlassParams.Default,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
) {
    when (blurMode) {
        BlurMode.Haze -> HazeAppBarBottomBar(blurRadiusDp, modifier)
        BlurMode.Cloudy -> CloudyAppBarBottomBar(blurRadiusDp, modifier)
        BlurMode.LiquidGlass -> LiquidGlassAppBarBottomBar(blurRadiusDp, liquidGlassParams, lightDirection, modifier)
    }
}

@Composable
private fun ImageGrid(
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
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
}

@Composable
private fun HazeAppBarBottomBar(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val style = HazeStyle(
        blurRadius = blurRadiusDp.dp,
        tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))),
    )

    Box(modifier = modifier.fillMaxSize()) {
        ImageGrid(
            modifier = Modifier.hazeSource(state = hazeState),
        )

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .hazeEffect(state = hazeState, style = style),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Gallery", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .hazeEffect(state = hazeState, style = style),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarIcons()
            }
        }
    }
}

@Composable
private fun CloudyAppBarBottomBar(
    blurRadiusDp: Float,
    modifier: Modifier = Modifier,
) {
    val cloudyRadius = hazeEquivalentCloudyRadius(blurRadiusDp)
    val tintColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)

    Box(modifier = modifier.fillMaxSize()) {
        ImageGrid()

        // Top bar: blurred background layer + clear content on top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clipToBounds(),
        ) {
            AsyncImage(
                model = SampleData.sampleImages.first(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .cloudy(radius = cloudyRadius),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tintColor),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Gallery", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Bottom bar: blurred background layer + clear icons on top
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .clipToBounds(),
        ) {
            AsyncImage(
                model = SampleData.sampleImages.last(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .cloudy(radius = cloudyRadius),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tintColor),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarIcons()
            }
        }
    }
}

@Composable
private fun LiquidGlassAppBarBottomBar(
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
        ImageGrid(
            modifier = Modifier.hazeSource(state = hazeState),
        )

        // Top bar: Haze backdrop blur + liquidGlass refraction
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            // Haze blur layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(state = hazeState, style = hazeStyle),
            )
            // LiquidGlass refraction layer
            LiquidGlassOverlay(params = params, cornerRadius = 0f, lightDirection = lightDirection)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Gallery", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Bottom bar: Haze backdrop blur + liquidGlass refraction
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(state = hazeState, style = hazeStyle),
            )
            LiquidGlassOverlay(params = params, cornerRadius = 0f, lightDirection = lightDirection)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarIcons()
            }
        }
    }
}

/** Transparent overlay that applies liquidGlass refraction on top of already-blurred content. */
@Composable
private fun LiquidGlassOverlay(
    params: LiquidGlassParams,
    cornerRadius: Float,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
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
private fun RowScope.BottomBarIcons() {
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
