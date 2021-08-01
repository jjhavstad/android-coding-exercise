package com.groundspeak.rove.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groundspeak.rove.datasources.api.ApiDestinationDataSource
import com.groundspeak.rove.datasources.local.LocalDestinationDataSource
import com.groundspeak.rove.models.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class DestinationViewModel(
    private val apiDestinationDataSource: ApiDestinationDataSource,
    private val localDestinationDataSource: LocalDestinationDataSource
) : ViewModel() {
    private val _destinationLiveData: MutableLiveData<List<Destination>> = MutableLiveData()

    val destinationLiveData: LiveData<List<Destination>>
        get() = _destinationLiveData

    var statusOnline: Boolean = true

    fun requestData() {
        when (statusOnline) {
            true -> requestRemoteData()
            false -> requestLocalData()
        }
    }

    private fun requestLocalData() {
        viewModelScope.launch(Dispatchers.IO) {
            localDestinationDataSource.request()?.let { _destinations ->
                _destinationLiveData.postValue(_destinations)
            }
        }
    }

    private fun requestRemoteData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                apiDestinationDataSource.request()?.let { _destinations ->
                    localDestinationDataSource.insert(_destinations)
                    _destinationLiveData.postValue(_destinations)
                }
            } catch (e: Exception) {
                e.message?.let { _errorMsg ->
                    Log.e(this.javaClass.name, _errorMsg)
                }
            }
        }
    }
}
