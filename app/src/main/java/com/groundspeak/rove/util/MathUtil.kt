package com.groundspeak.rove.util

import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MathUtil {
    companion object {
        val EARTH_RADIUS = 6371009.0

        fun clamp(x: Double, low: Double, high: Double): Double = when {
            x < low -> low
            x > high -> high
            else -> x
        }

        fun wrap(n: Double, min: Double, max: Double): Double = if (n >= min && n < max) {
            n
        } else {
            ((n - min).mod(max - min) + min)
        }

        fun mercator(lat: Double): Double = ln(tan(lat * 0.5 + PI / 4.0))

        fun inverseMeractor(y: Double): Double = 2 * atan(exp(y)) - PI / 2.0

        fun hav(x: Double): Double = sin(x * 0.5).let { it * it }

        fun arcHav(x: Double): Double = 2.0 * asin(sqrt(x))

        fun sinFromHav(h: Double): Double = 2.0 * sqrt(h * (1.0 - h))

        fun havFromSin(x: Double): Double = (x * x).let {
            it / (1.0 + sqrt(1.0 - it)) * 0.5
        }

        fun sinSumFromHav(x: Double, y: Double): Double = sqrt(x * (1.0 - x)).let { a ->
            sqrt(y * (1.0 - y)).let { b ->
                2 * (a + b - 2 * (a * y + b * x))
            }
        }

        fun havDistance(lat1: Double, lat2: Double, dLng: Double): Double =
            hav(lat1 - lat2) + hav(dLng) * cos(lat1) * cos(lat2)
    }
}
