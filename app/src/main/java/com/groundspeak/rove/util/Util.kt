package com.groundspeak.rove.util

import java.util.Locale

class Util {
    companion object {
        private val METRE_CUTOFF = 500
        fun getDistanceString(meters: Double): String {
            return if (meters < METRE_CUTOFF) {
                String.format(Locale.getDefault(), "%.0fm", meters)
            } else {
                (meters / 1000).let { _km ->
                    String.format(Locale.getDefault(), "%.1fkm", _km)
                }
            }
        }
    }
}
