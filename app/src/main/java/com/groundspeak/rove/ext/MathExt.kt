package com.groundspeak.rove.ext

import kotlin.math.PI

fun Float.toRadians(): Float = PI.toFloat() * this / 180.0f

fun Float.toDegrees(): Float = 180.0f * this / PI.toFloat()

fun Double.toRadians(): Double = PI * this / 180.0

fun Double.toDegrees(): Double = 180.0 * this / PI
