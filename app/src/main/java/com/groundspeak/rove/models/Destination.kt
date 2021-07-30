package com.groundspeak.rove.models

data class Destination(
    val id: Long,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val message: String
)
