package com.groundspeak.rove.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.groundspeak.rove.util.Compass
import com.groundspeak.rove.util.OrientationSensorListener

class OrientationSensorViewModel : ViewModel() {
    private val _headingLiveData: MutableLiveData<Float> = MutableLiveData()
    val headingLiveData: LiveData<Float>
        get() = _headingLiveData

    private val compassListener = OrientationSensorListener {
        _headingLiveData.postValue(it)
    }

    private var compass: Compass? = null

    fun initializeCompass(context: Context) {
        compass = Compass(context, compassListener)
    }

    fun start() {
        compass?.start()
    }

    fun stop() {
        compass?.stop()
    }
}
