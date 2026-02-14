package com.example.liquidglasslab.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class DeviceMotionData(
    val pitch: Float = 0f, // tilt forward/backward (radians)
    val roll: Float = 0f,  // tilt left/right (radians)
)

/**
 * Composable that provides smoothed device pitch/roll from TYPE_ROTATION_VECTOR sensor.
 * Uses exponential smoothing to reduce jitter.
 *
 * @param smoothingFactor 0..1, higher = more smoothing (slower response). Default 0.6.
 */
@Composable
fun rememberDeviceMotion(smoothingFactor: Float = 0.6f): State<DeviceMotionData> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(DeviceMotionData()) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            return@DisposableEffect onDispose { }
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var smoothedPitch = 0f
        var smoothedRoll = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // orientation[1] = pitch, orientation[2] = roll
                val rawPitch = orientation[1]
                val rawRoll = orientation[2]

                // Exponential moving average
                smoothedPitch = smoothingFactor * smoothedPitch + (1f - smoothingFactor) * rawPitch
                smoothedRoll = smoothingFactor * smoothedRoll + (1f - smoothingFactor) * rawRoll

                state.value = DeviceMotionData(
                    pitch = smoothedPitch,
                    roll = smoothedRoll,
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI,
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return state
}
