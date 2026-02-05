package com.example.liquidglasslab.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.liquidglasslab.patterns.ABTogglePattern
import com.example.liquidglasslab.patterns.AppBarBottomBarPattern
import com.example.liquidglasslab.patterns.FloatingCardPattern
import com.example.liquidglasslab.patterns.FullScreenOverlayPattern
import com.example.liquidglasslab.patterns.LibraryType
import com.example.liquidglasslab.patterns.PatternType
import com.example.liquidglasslab.patterns.SplitViewPattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen() {
    var selectedPatternIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedLibraryIndex by rememberSaveable { mutableIntStateOf(0) }

    val patterns = PatternType.entries
    val libraries = LibraryType.entries

    Column(modifier = Modifier.fillMaxSize()) {
        // Pattern selector tabs
        TabRow(
            selectedTabIndex = selectedPatternIndex,
        ) {
            patterns.forEachIndexed { index, pattern ->
                Tab(
                    selected = selectedPatternIndex == index,
                    onClick = { selectedPatternIndex = index },
                    text = {
                        Text(
                            text = pattern.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                        )
                    },
                )
            }
        }

        // Library selector (only for non-comparison patterns)
        val selectedPattern = patterns[selectedPatternIndex]
        val showLibrarySelector = selectedPattern !in listOf(
            PatternType.ABToggle,
            PatternType.SplitView,
        )

        if (showLibrarySelector) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                libraries.forEachIndexed { index, library ->
                    SegmentedButton(
                        selected = selectedLibraryIndex == index,
                        onClick = { selectedLibraryIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = libraries.size,
                        ),
                    ) {
                        Text(library.label)
                    }
                }
            }
        }

        // Pattern content
        val selectedLibrary = libraries[selectedLibraryIndex]
        when (selectedPattern) {
            PatternType.AppBarBottomBar -> AppBarBottomBarPattern(
                libraryType = selectedLibrary,
                modifier = Modifier.fillMaxSize(),
            )
            PatternType.FloatingCard -> FloatingCardPattern(
                libraryType = selectedLibrary,
                modifier = Modifier.fillMaxSize(),
            )
            PatternType.FullScreenOverlay -> FullScreenOverlayPattern(
                libraryType = selectedLibrary,
                modifier = Modifier.fillMaxSize(),
            )
            PatternType.ABToggle -> ABTogglePattern(
                modifier = Modifier.fillMaxSize(),
            )
            PatternType.SplitView -> SplitViewPattern(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
