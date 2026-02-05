package com.example.liquidglasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.liquidglasslab.screens.ComparisonScreen
import com.example.liquidglasslab.screens.PlaygroundScreen
import com.example.liquidglasslab.theme.LiquidGlassLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiquidGlassLabTheme {
                MainScreen()
            }
        }
    }
}

enum class AppMode(val label: String) {
    Playground("Playground"),
    Comparison("Comparison"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedModeIndex by rememberSaveable { mutableIntStateOf(0) }
    val modes = AppMode.entries

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
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
            },
        )

        when (modes[selectedModeIndex]) {
            AppMode.Playground -> PlaygroundScreen()
            AppMode.Comparison -> ComparisonScreen()
        }
    }
}
