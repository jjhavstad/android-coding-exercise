package com.groundspeak.rove.util

import android.location.Location
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

fun interface OnLocationReceived {
    operator fun invoke(location: Location)
}

fun interface OnLocationError {
    operator fun invoke()
}

class OnLocationCallback(
    private val onLocationReceived: OnLocationReceived,
    private val onLocationError: OnLocationError
) : LocationCallback() {
    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        if (!locationAvailability.isLocationAvailable) {
            onLocationError()
        }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        onLocationReceived(locationResult.lastLocation)
    }
}
