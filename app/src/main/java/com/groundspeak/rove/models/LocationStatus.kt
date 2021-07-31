package com.groundspeak.rove.models

data class LocationStatus(
    val distance: Double,
    val heading: Float,
    val hasReachedDestination: Boolean
)
