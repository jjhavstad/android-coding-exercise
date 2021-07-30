package com.groundspeak.rove

import android.content.Context
import android.content.Intent
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
import org.koin.android.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference

class PrimerActivity : AppCompatActivity() {
    //TODO Request location permission at runtime on API >= 21
    //TODO show magic location settings dialog (if necessary)

    private val weakThis: WeakReference<PrimerActivity> = WeakReference(this)

    private lateinit var binding: ActivityPrimerBinding

    private val destinationViewModel: DestinationViewModel by viewModel()

    private val destinationLiveDataObserver = Observer<List<Destination>> {
        weakThis.get()?.onDestinationListReceived(it)
    }

    private var nextDestination: Destination? = null

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
    }

    override fun onResume() {
        super.onResume()
        destinationViewModel.requestData()
    }

    private fun onDestinationListReceived(destinations: List<Destination>) {
        if (!destinations.isEmpty()) {
            nextDestination = destinations[0]
            binding.textView.visibility = View.VISIBLE
            binding.buttonGo.enable()
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, PrimerActivity::class.java)
    }
}
