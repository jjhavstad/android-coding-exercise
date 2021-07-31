package com.groundspeak.rove.util

fun interface OrientationSensorListener {
    fun onHeadingUpdate(heading: Float)
}

interface OrientationSensor {
    val listener: OrientationSensorListener
    fun start()
    fun stop()
}
