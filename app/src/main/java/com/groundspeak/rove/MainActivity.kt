package com.groundspeak.rove

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.groundspeak.rove.databinding.ActivityMainBinding
import com.groundspeak.rove.models.LocationStatus
import com.groundspeak.rove.util.Compass
import com.groundspeak.rove.util.LatLng
import com.groundspeak.rove.util.Util
import com.groundspeak.rove.viewmodels.LocationViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var destinationMessage: String

    private val weakThis: WeakReference<MainActivity> = WeakReference(this)

    private val locationViewModel: LocationViewModel by viewModel()
    private val locationLiveDataObserver = Observer<LocationStatus> {
        weakThis.get()?.apply {
            binding.statusMessage.text = null
            binding.distance.text = Util.getDistanceString(it.distance)
            val matrix = Matrix()
            val bounds = binding.compassArrow.drawable.bounds
            matrix.postRotate(
                it.heading,
                bounds.width() / 2.0f,
                bounds.height() / 2.0f
            )
            binding.compassArrow.scaleType = ImageView.ScaleType.MATRIX
            binding.compassArrow.imageMatrix = matrix
            if (it.hasReachedDestination) {
                startActivity(
                    DoneActivity.createIntent(
                        context = this,
                        targetMessage = destinationMessage
                    )
                )
                finish()
            }
        }
    }

    private val locationErrorLiveDataObserver = Observer<Unit> {
        weakThis.get()?.apply {
            binding.statusMessage.setText(R.string.location_error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val latitude = intent?.getDoubleExtra(EXTRA_TARGET_LAT, 0.0) ?: 0.0
        val longitude = intent?.getDoubleExtra(EXTRA_TARGET_LNG, 0.0) ?: 0.0
        destinationMessage = intent?.getStringExtra(EXTRA_TARGET_MESSAGE) ?: ""

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        //for now, assuming phone is set to optimal gps settings
        locationRequest = LocationRequest.create().also {
            it.interval = 2000
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        binding.cancelAction.setOnClickListener {
            finish()
        }

        locationViewModel.initalizeTargetLocation(
            targetLocation =  LatLng(
                latitude = latitude,
                longitude = longitude
            ),
            targetRadius = DEFAULT_DISTANCE_TO_TARGET_THRESHOLD
        )
        locationViewModel.locationLiveData.observe(
            this,
            locationLiveDataObserver
        )
        locationViewModel.locationErrorLiveData.observe(
            this,
            locationErrorLiveDataObserver
        )
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
            locationViewModel.locationCallback,
            null
        )
        if (locationViewModel.orientationSensor == null) {
            locationViewModel.orientationSensor = Compass.Builder(locationViewModel.orientationSensorListener)
                .context(this)
                .build()
        }
        locationViewModel.startCompass()
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationViewModel.locationCallback)
        locationViewModel.stopCompass()
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

    companion object {
        val EXTRA_TARGET_LAT = "MainActivity.TARGET_LATITUDE"
        val EXTRA_TARGET_LNG = "MainActivity.TARGET_LONGITUDE"
        val EXTRA_TARGET_MESSAGE = "MainActivity.TARGET_MESSAGE"
        val EXTRA_RADIUS = "MainActivity.RADIUS" //distance (meters) at which to show message

        private val REQUEST_LOCATION_PERMISSION_CODE = 1000
        private val DEFAULT_DISTANCE_TO_TARGET_THRESHOLD = 5.0 // distance in meters

        fun createIntent(context: Context, latLng: LatLng, message: String): Intent {
            return Intent(context, MainActivity::class.java).also {
                it.putExtra(EXTRA_TARGET_LAT, latLng.latitude)
                it.putExtra(EXTRA_TARGET_LNG, latLng.longitude)
                it.putExtra(EXTRA_TARGET_MESSAGE, message)
            }
        }
    }
}
