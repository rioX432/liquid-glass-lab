package com.example.liquidglasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.liquidglasslab.components.ParameterPanel
import com.example.liquidglasslab.sensor.rememberDeviceMotion
import com.example.liquidglasslab.patterns.AppBarBottomBarPattern
import com.example.liquidglasslab.patterns.BlurMode
import com.example.liquidglasslab.patterns.FloatingCardPattern
import com.example.liquidglasslab.patterns.FullScreenOverlayPattern
import com.example.liquidglasslab.patterns.PatternType
import com.example.liquidglasslab.theme.LiquidGlassLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiquidGlassLabTheme {
                LabScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen() {
    var selectedPatternIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedModeIndex by rememberSaveable { mutableIntStateOf(0) }
    var blurRadiusDp by rememberSaveable { mutableFloatStateOf(24f) }
    var liquidGlassParams by remember { mutableStateOf(LiquidGlassParams.Default) }

    val patterns = PatternType.entries
    val modes = BlurMode.entries
    val selectedMode = modes[selectedModeIndex]

    // Gyroscope → light direction
    val motion by rememberDeviceMotion()
    val defaultLightDir = Offset(-0.707f, -0.707f)
    val lightDirection = if (liquidGlassParams.enableGyroscope) {
        Offset(
            (motion.roll * 1.5f).coerceIn(-1f, 1f),
            (motion.pitch * 1.5f).coerceIn(-1f, 1f),
        )
    } else {
        defaultLightDir
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Liquid Glass Lab") })

        // Pattern TabRow
        TabRow(selectedTabIndex = selectedPatternIndex) {
            patterns.forEachIndexed { index, pattern ->
                Tab(
                    selected = selectedPatternIndex == index,
                    onClick = { selectedPatternIndex = index },
                    text = { Text(pattern.label) },
                )
            }
        }

        // BlurMode SegmentedButton
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            modes.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selectedModeIndex == index,
                    onClick = { selectedModeIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = modes.size,
                    ),
                ) {
                    Text(mode.label)
                }
            }
        }

        // Blur Radius Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Blur",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = blurRadiusDp,
                onValueChange = { blurRadiusDp = it },
                valueRange = 0f..50f,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${blurRadiusDp.toInt()} dp",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // Glass Parameter Panel (LiquidGlass mode only)
        AnimatedVisibility(
            visible = selectedMode == BlurMode.LiquidGlass,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ParameterPanel(
                params = liquidGlassParams,
                onParamsChange = { liquidGlassParams = it },
                lightDirection = lightDirection,
                motionData = motion,
            )
        }

        // Pattern Content
        Crossfade(
            targetState = patterns[selectedPatternIndex] to selectedMode,
            label = "pattern-crossfade",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) { (pattern, mode) ->
            when (pattern) {
                PatternType.AppBarBottomBar -> AppBarBottomBarPattern(
                    blurMode = mode,
                    blurRadiusDp = blurRadiusDp,
                    liquidGlassParams = liquidGlassParams,
                    lightDirection = lightDirection,
                )
                PatternType.FloatingCard -> FloatingCardPattern(
                    blurMode = mode,
                    blurRadiusDp = blurRadiusDp,
                    liquidGlassParams = liquidGlassParams,
                    lightDirection = lightDirection,
                )
                PatternType.FullScreenOverlay -> FullScreenOverlayPattern(
                    blurMode = mode,
                    blurRadiusDp = blurRadiusDp,
                    liquidGlassParams = liquidGlassParams,
                    lightDirection = lightDirection,
                )
            }
        }
    }
}
