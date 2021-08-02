package com.groundspeak.rove

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.groundspeak.rove.databinding.ActivityPrimerBinding
import com.groundspeak.rove.ext.disable
import com.groundspeak.rove.ext.enable
import com.groundspeak.rove.models.Destination
import com.groundspeak.rove.util.LatLng
import com.groundspeak.rove.viewmodels.DestinationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

class PrimerActivity : AppCompatActivity(), KoinComponent {
    //TODO Request location permission at runtime on API >= 21
    //TODO show magic location settings dialog (if necessary)

    private val weakThis: WeakReference<PrimerActivity> = WeakReference(this)

    private lateinit var binding: ActivityPrimerBinding

    private val destinationViewModel: DestinationViewModel by viewModel()

    private val destinationLiveDataObserver = Observer<List<Destination>> {
        weakThis.get()?.onDestinationListReceived(it)
    }

    private var nextDestination: Destination? = null

    private val networkStateListener: NetworkStateListener by inject()
    private val onNetworkStateChangeListener = NetworkStateListener.OnStateChangeListener {
        weakThis.get()?.apply {
            destinationViewModel.statusOnline = it
            runOnUiThread {
                binding.offlineImg.visibility = if (it) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.visibility = View.INVISIBLE

        binding.buttonGo.disable()
        binding.buttonGo.setOnClickListener {
            nextDestination?.let {
                startActivity(
                    MainActivity.createIntent(
                        context = this,
                        latLng = LatLng(latitude = it.latitude, longitude = it.longitude),
                        message = it.message
                    )
                )
            }
        }

        destinationViewModel.destinationLiveData.observe(this, destinationLiveDataObserver)

        networkStateListener.addOnStateChangeListener(onNetworkStateChangeListener)
    }

    override fun onResume() {
        super.onResume()
        registerNetworkStateChangeListener()
        destinationViewModel.requestData()
    }

    override fun onPause() {
        super.onPause()
        unregisterNetworkStateChangeListener()
    }

    private fun onDestinationListReceived(destinations: List<Destination>) {
        if (!destinations.isEmpty()) {
            nextDestination = destinations[0]
            binding.textView.visibility = View.VISIBLE
            binding.buttonGo.enable()
        }
    }

    private fun registerNetworkStateChangeListener() {
        val connectivityManager: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork != null
        } else {
            connectivityManager.activeNetworkInfo != null &&
                    connectivityManager.activeNetworkInfo?.isConnected == true
        }
        onNetworkStateChangeListener.sendNetworkStatus(networkConnected)
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
            networkStateListener
        )
    }

    private fun unregisterNetworkStateChangeListener() {
        val connectivityManager: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkStateListener)
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, PrimerActivity::class.java)
    }
}
