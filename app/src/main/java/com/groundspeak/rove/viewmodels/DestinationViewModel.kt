package com.groundspeak.rove.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groundspeak.rove.datasources.api.ApiDestinationDataSource
import com.groundspeak.rove.models.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class DestinationViewModel(
    private val apiDestinationDataSource: ApiDestinationDataSource
) : ViewModel() {
    private val _destinationLiveData: MutableLiveData<List<Destination>> = MutableLiveData()

    val destinationLiveData: LiveData<List<Destination>>
        get() = _destinationLiveData

    fun requestData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _destinationLiveData.postValue(apiDestinationDataSource.request())
            } catch (e: Exception) {
                e.message?.let { _errorMsg ->
                    Log.e(this.javaClass.name, _errorMsg)
                }
            }
        }
    }
}
