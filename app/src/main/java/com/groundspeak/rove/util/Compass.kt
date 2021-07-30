package com.groundspeak.rove.util

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import java.lang.ref.WeakReference
import kotlin.math.PI

class Compass(context: Context, val listener: CompassListener) {
    // Heading = direction device is pointing, relative to North (clockwise)
    // Bearing = direction to destination, relative to North (clockwise)
    // Relative bearing = angle between Heading and Bearing (clockwise) (i.e. Bearing minus Heading)

    interface CompassListener {
        fun onHeadingUpdate(heading: Float)
    }

    private val weakThis: WeakReference<Compass> = WeakReference(this)

    private val sensorManager: SensorManager
    private val display: Display?
    private val sensorEventListener: SensorEventListener

    private var gravityVector = FloatArray(3)
    private var magneticFieldVector = FloatArray(3)
    private val orientation = FloatArray(3)
    private val rotationMatrix = FloatArray(9)

    private val onUpdateGravityVector = OnUpdateGravityVector {
        gravityVector = lowPass(it, gravityVector)
    }

    private val onUpdateMagneticFieldVector = OnUpdateMagneticFieldVector {
        magneticFieldVector = lowPass(it, magneticFieldVector)
    }

    private val onUpdateCoordinateSystem = OnUpdateCoordinateSystem { x, y ->
        val fixedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, x, y, fixedRotationMatrix)
        SensorManager.getOrientation(fixedRotationMatrix, orientation)
        weakThis.get()?.listener?.onHeadingUpdate(180.0f * orientation[0] / PI.toFloat())
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }

        sensorEventListener = CompassSensorEventListener(
            onUpdateGravityVector,
            onUpdateMagneticFieldVector,
            onUpdateCoordinateSystem,
            display
        )
    }

    fun start() {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stop() {
        sensorManager.unregisterListener(
            sensorEventListener, sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )
        )
        sensorManager.unregisterListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        )
    }

    fun interface OnUpdateGravityVector {
        operator fun invoke(eventValues: FloatArray)
    }

    fun interface OnUpdateMagneticFieldVector {
        operator fun invoke(eventValues: FloatArray)
    }

    fun interface OnUpdateCoordinateSystem {
        operator fun invoke(x: Int, y: Int)
    }

    private class CompassSensorEventListener(
        val onUpdateGravityVector: OnUpdateGravityVector,
        val onUpdateMagneticFieldVector: OnUpdateMagneticFieldVector,
        val onUpdateCoordinateSystem: OnUpdateCoordinateSystem,
        val display: Display?
    ) : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.sensor?.let { _eventSensor ->
                display?.rotation.let { _rotation ->
                    when (_eventSensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> onUpdateGravityVector(event.values.clone())
                        Sensor.TYPE_MAGNETIC_FIELD -> onUpdateMagneticFieldVector(event.values.clone())
                    }
                    var x = SensorManager.AXIS_X
                    var y = SensorManager.AXIS_Y
                    when (_rotation) {
                        Surface.ROTATION_90 -> {
                            x = SensorManager.AXIS_Y
                            y = SensorManager.AXIS_MINUS_X
                        }
                        Surface.ROTATION_180 -> {
                            x = SensorManager.AXIS_X
                            y = SensorManager.AXIS_Y
                        }
                        Surface.ROTATION_270 -> {
                            x = SensorManager.AXIS_MINUS_Y
                            y = SensorManager.AXIS_X
                        }
                    }
                    onUpdateCoordinateSystem(x, y)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    companion object {
        private val LOW_PASS_ALPHA = 0.15f

        fun adjustHeadingForDeclination(heading: Float, location: Location?): Float {
            var declination = 0.0f
            location?.let {
                val field = GeomagneticField(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    location.altitude.toFloat(),
                    System.currentTimeMillis()
                )
                declination = field.declination
            }
            return heading * declination
        }

        private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
            return output?.also { _output ->
               for (i in 0 until Math.min(_output.size, input.size)) {
                   _output[i] = _output[i] + LOW_PASS_ALPHA * (input[i] - _output[i])
               }
            } ?: input
        }
    }
}
