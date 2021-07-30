package com.groundspeak.rove

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.groundspeak.rove.databinding.ActivityMainBinding
import com.groundspeak.rove.util.LatLng
import com.groundspeak.rove.util.SphericalUtil
import com.groundspeak.rove.util.Util
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var targetLatLng: LatLng

    private val weakThis: WeakReference<MainActivity> = WeakReference(this)

    private val onLocationReceived = OnLocationReceived { _location ->
        weakThis.get()?.apply {
            binding.statusMessage.text = null

            val userLatLng = LatLng(
                latitude = _location.latitude,
                longitude = _location.longitude
            )

            binding.distance.text = Util.getDistanceString(
                SphericalUtil.computeDistanceBetween(userLatLng, targetLatLng)
            )

            val matrix = Matrix()
            val bounds = binding.compassArrow.drawable.bounds
            matrix.postRotate(
                SphericalUtil.computeHeading(userLatLng, targetLatLng).toFloat(),
                bounds.width() / 2.0f,
                bounds.height() / 2.0f
            )
            binding.compassArrow.scaleType = ImageView.ScaleType.MATRIX
            binding.compassArrow.imageMatrix = matrix
        }
    }

    private val onLocationError = OnLocationError {
        weakThis.get()?.apply {
            binding.statusMessage.setText(R.string.location_error);
        }
    }

    private val locationCallback = OnLocationCallback(onLocationReceived, onLocationError)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val latitude = intent?.getDoubleExtra(EXTRA_TARGET_LAT, 0.0) ?: 0.0
        val longitude = intent?.getDoubleExtra(EXTRA_TARGET_LNG, 0.0) ?: 0.0

        targetLatLng = LatLng(
            latitude = latitude,
            longitude = longitude
        )

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        //for now, assuming phone is set to optimal gps settings
        locationRequest = LocationRequest.create().also {
            it.interval = 2000
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        binding.cancelAction.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        requestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.firstOrNull {
            it == PackageManager.PERMISSION_GRANTED
        }?.let {
            requestLocationUpdatesFromClient()
        }
    }

    private fun requestLocationUpdates() {
        if (!locationPermissionsGranted()) {
            requestLocationPermissions()
        } else {
            requestLocationUpdatesFromClient()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdatesFromClient() {
        binding.statusMessage.setText(R.string.location_pending)
        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    private fun locationPermissionsGranted(): Boolean = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        }
    }

    fun interface OnLocationReceived {
        operator fun invoke(location: Location)
    }

    fun interface OnLocationError {
        operator fun invoke()
    }

    private class OnLocationCallback(
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

    companion object {
        val EXTRA_TARGET_LAT = "MainActivity.TARGET_LATITUDE"
        val EXTRA_TARGET_LNG = "MainActivity.TARGET_LONGITUDE"
        val EXTRA_TARGET_MESSAGE = "MainActivity.TARGET_MESSAGE"
        val EXTRA_RADIUS = "MainActivity.RADIUS" //distance (meters) at which to show message

        private val REQUEST_LOCATION_PERMISSION_CODE = 1000

        fun createIntent(context: Context, latLng: LatLng, message: String): Intent {
            return Intent(context, MainActivity::class.java).also {
                it.putExtra(EXTRA_TARGET_LAT, latLng.latitude)
                it.putExtra(EXTRA_TARGET_LNG, latLng.longitude)
                it.putExtra(EXTRA_TARGET_MESSAGE, message)
            }
        }
    }
}
