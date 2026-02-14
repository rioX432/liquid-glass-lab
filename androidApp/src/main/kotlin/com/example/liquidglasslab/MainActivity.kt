package com.example.liquidglasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    val patterns = PatternType.entries
    val modes = BlurMode.entries
    val selectedMode = modes[selectedModeIndex]

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
                )
                PatternType.FloatingCard -> FloatingCardPattern(
                    blurMode = mode,
                    blurRadiusDp = blurRadiusDp,
                )
                PatternType.FullScreenOverlay -> FullScreenOverlayPattern(
                    blurMode = mode,
                    blurRadiusDp = blurRadiusDp,
                )
            }
        }
    }
}
