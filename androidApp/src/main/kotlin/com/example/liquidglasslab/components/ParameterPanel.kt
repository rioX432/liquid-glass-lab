package com.example.liquidglasslab.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.liquidglasslab.LiquidGlassParams
import com.example.liquidglasslab.sensor.DeviceMotionData

@Composable
fun ParameterPanel(
    params: LiquidGlassParams,
    onParamsChange: (LiquidGlassParams) -> Unit,
    modifier: Modifier = Modifier,
    lightDirection: Offset = Offset(-0.707f, -0.707f),
    motionData: DeviceMotionData = DeviceMotionData(),
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Glass Parameters",
                style = MaterialTheme.typography.titleSmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expanded) {
                    TextButton(onClick = { onParamsChange(LiquidGlassParams.Default) }) {
                        Text("Reset", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
        }

        // Sliders
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // --- Phase 1: Base params ---
                SectionLabel("Base")
                ParamSlider(
                    label = "Refraction",
                    value = params.refraction,
                    range = 0f..1f,
                    onValueChange = { onParamsChange(params.copy(refraction = it)) },
                )
                ParamSlider(
                    label = "Curve",
                    value = params.curve,
                    range = 0f..1f,
                    onValueChange = { onParamsChange(params.copy(curve = it)) },
                )
                ParamSlider(
                    label = "Dispersion",
                    value = params.dispersion,
                    range = 0f..0.5f,
                    onValueChange = { onParamsChange(params.copy(dispersion = it)) },
                )
                ParamSlider(
                    label = "Saturation",
                    value = params.saturation,
                    range = 0.5f..2f,
                    onValueChange = { onParamsChange(params.copy(saturation = it)) },
                )
                ParamSlider(
                    label = "Contrast",
                    value = params.contrast,
                    range = 0.5f..2f,
                    onValueChange = { onParamsChange(params.copy(contrast = it)) },
                )
                ParamSlider(
                    label = "Tint Alpha",
                    value = params.tintAlpha,
                    range = 0f..0.5f,
                    onValueChange = { onParamsChange(params.copy(tintAlpha = it)) },
                )
                ParamSlider(
                    label = "Edge",
                    value = params.edge,
                    range = 0f..1f,
                    onValueChange = { onParamsChange(params.copy(edge = it)) },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // --- Phase 2: Specular ---
                ToggleableSection(
                    label = "Specular",
                    enabled = params.enableSpecular,
                    onToggle = { onParamsChange(params.copy(enableSpecular = it)) },
                )
                if (params.enableSpecular) {
                    ParamSlider(
                        label = "Amplitude",
                        value = params.specularAmplitude,
                        range = 0f..5f,
                        onValueChange = { onParamsChange(params.copy(specularAmplitude = it)) },
                    )
                    ParamSlider(
                        label = "Exponent",
                        value = params.specularExponent,
                        range = 1f..20f,
                        onValueChange = { onParamsChange(params.copy(specularExponent = it)) },
                    )
                }

                // --- Phase 2: Fresnel ---
                ToggleableSection(
                    label = "Fresnel",
                    enabled = params.enableFresnel,
                    onToggle = { onParamsChange(params.copy(enableFresnel = it)) },
                )
                if (params.enableFresnel) {
                    ParamSlider(
                        label = "Width",
                        value = params.fresnelWidth,
                        range = 0f..20f,
                        onValueChange = { onParamsChange(params.copy(fresnelWidth = it)) },
                    )
                    ParamSlider(
                        label = "Blur",
                        value = params.fresnelBlur,
                        range = 0f..20f,
                        onValueChange = { onParamsChange(params.copy(fresnelBlur = it)) },
                    )
                }

                // --- Phase 2: Noise ---
                ToggleableSection(
                    label = "Noise",
                    enabled = params.enableNoise,
                    onToggle = { onParamsChange(params.copy(enableNoise = it)) },
                )
                if (params.enableNoise) {
                    ParamSlider(
                        label = "Intensity",
                        value = params.noiseIntensity,
                        range = 0f..0.1f,
                        onValueChange = { onParamsChange(params.copy(noiseIntensity = it)) },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // --- Phase 3: Gyroscope ---
                ToggleableSection(
                    label = "Gyroscope",
                    enabled = params.enableGyroscope,
                    onToggle = { onParamsChange(params.copy(enableGyroscope = it)) },
                )
                if (params.enableGyroscope) {
                    Text(
                        text = "pitch=%.2f  roll=%.2f  light=(%.2f, %.2f)".format(
                            motionData.pitch, motionData.roll,
                            lightDirection.x, lightDirection.y,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 48.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun ToggleableSection(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ParamSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.3f),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(0.55f),
        )
        Text(
            text = "%.2f".format(value),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(0.15f),
        )
    }
}
