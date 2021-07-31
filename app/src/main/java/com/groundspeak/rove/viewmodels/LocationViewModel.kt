package com.groundspeak.rove.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.groundspeak.rove.models.LocationStatus
import com.groundspeak.rove.util.LatLng
import com.groundspeak.rove.util.OrientationSensorListener
import com.groundspeak.rove.util.Compass
import com.groundspeak.rove.util.OnLocationCallback
import com.groundspeak.rove.util.OrientationSensor
import com.groundspeak.rove.util.OnLocationError
import com.groundspeak.rove.util.OnLocationReceived
import com.groundspeak.rove.util.SphericalUtil

class LocationViewModel : ViewModel() {
    private var heading: Float? = null
    private var targetLatLng: LatLng? = null
    private var distanceToTargetThreshold: Double? = null

    private val _locationLiveData: MutableLiveData<LocationStatus> = MutableLiveData()
    val locationLiveData: LiveData<LocationStatus>
        get() = _locationLiveData

    private val _locationErrorLiveData: MutableLiveData<Unit> = MutableLiveData()
    val locationErrorLiveData: LiveData<Unit>
        get() = _locationErrorLiveData

    val orientationSensorListener = OrientationSensorListener {
        heading = it
    }

    var orientationSensor: OrientationSensor? = null

    private val onLocationReceived = OnLocationReceived { _location ->
        targetLatLng?.let { _targetLatLng ->
            distanceToTargetThreshold?.let { _distanceToTargetThreshold ->
                val userLatLng = LatLng(
                    latitude = _location.latitude,
                    longitude = _location.longitude
                )

                val distanceBetween = SphericalUtil.computeDistanceBetween(userLatLng, _targetLatLng)
                val trueBearing = SphericalUtil.computeHeading(userLatLng, _targetLatLng).toFloat()
                val trueCourse = heading ?: 0.0f
                val relativeBearing = trueBearing - trueCourse
                _locationLiveData.postValue(
                    LocationStatus(
                        distance = distanceBetween,
                        heading = relativeBearing,
                        hasReachedDestination = distanceBetween <= _distanceToTargetThreshold
                    )
                )
            }
        }
    }

    private val onLocationError = OnLocationError {
        _locationErrorLiveData.postValue(Unit)
    }

    val locationCallback = OnLocationCallback(onLocationReceived, onLocationError)

    fun initalizeTargetLocation(targetLocation: LatLng, targetRadius: Double) {
        targetLatLng = targetLocation
        distanceToTargetThreshold = targetRadius
    }

    fun startCompass() {
        orientationSensor?.start()
    }

    fun stopCompass() {
        orientationSensor?.stop()
    }
}
