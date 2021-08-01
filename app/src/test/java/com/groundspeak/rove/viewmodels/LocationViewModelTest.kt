package com.groundspeak.rove.viewmodels

import android.location.Location
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.groundspeak.rove.models.LocationStatus
import com.groundspeak.rove.util.InstantExecutorListener
import com.groundspeak.rove.util.LatLng
import com.groundspeak.rove.util.OrientationSensor
import com.groundspeak.rove.util.OrientationSensorListener
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class LocationViewModelTest : DescribeSpec({
    describe("LocationViewModel", {
        val locationObserver: Observer<LocationStatus> = mock()
        val locationErrorObserver: Observer<Unit> = mock()
        val locationViewModel = LocationViewModel()
        beforeEach {
            locationViewModel.locationLiveData.observeForever(locationObserver)
            locationViewModel.locationErrorLiveData.observeForever(locationErrorObserver)
        }
        describe("when the view model has been initialized with a target location", {
            val mockTargetLatLng = LatLng(latitude = 47.686299, longitude = -122.135353)
            val mockTargetRadius = 5.0
            beforeEach {
                locationViewModel.initalizeTargetLocation(mockTargetLatLng, mockTargetRadius)
            }
            describe("when a location error is returned from the location callback", {
                val mockLocationAvailability: LocationAvailability = mock()
                beforeEach {
                    whenever(mockLocationAvailability.isLocationAvailable).thenReturn(false)
                    locationViewModel.locationCallback.onLocationAvailability(mockLocationAvailability)
                }
                it("should post a result to the location error live data", {
                    verify(locationErrorObserver).onChanged(Unit)
                })
            })
            describe("when a location is returned from the location callback", {
                val mockUserLat = 47.7
                val mockUserLon = -122.135353
                val mockLocationResult: LocationResult = mock()
                val mockLocation: Location = mock()
                val expectedResult = LocationStatus(
                    distance = 1523.4838421061422,
                    heading = 0.0f,
                    hasReachedDestination = false
                )
                beforeEach {
                    whenever(mockLocationResult.lastLocation).thenReturn(mockLocation)
                    whenever(mockLocation.latitude).thenReturn(mockUserLat)
                    whenever(mockLocation.longitude).thenReturn(mockUserLon)
                }
                describe("when the view model has not been initialized with a compass", {
                    beforeEach {
                        locationViewModel.locationCallback.onLocationResult(mockLocationResult)
                    }
                    it("should post a location satus result with a location status result", {
                        verify(locationObserver).onChanged(expectedResult)
                    })
                })
                describe("when the view model has been initialized with a compass", {
                    val mockHeading = 30.0f
                    val mockOrientationSensor = object: OrientationSensor {
                        override val listener: OrientationSensorListener
                            get() = locationViewModel.orientationSensorListener

                        override fun start() {
                            // no-op
                        }

                        override fun stop() {
                            // no-op
                        }
                    }
                    val argumentCaptor = argumentCaptor<LocationStatus>()
                    val expectedResultWithRelativeBearing = LocationStatus(
                        distance = 1523.4838421061422,
                        heading = -210.0f,
                        hasReachedDestination = false
                    )
                    beforeEach {
                        locationViewModel.orientationSensor = mockOrientationSensor
                        mockOrientationSensor.listener.onHeadingUpdate(mockHeading)
                        locationViewModel.locationCallback.onLocationResult(mockLocationResult)
                    }
                    it("should post a location status result with a non-zero heading", {
                        verify(locationObserver, times(2)).onChanged(argumentCaptor.capture())
                        argumentCaptor.allValues[0] shouldBe expectedResult
                        argumentCaptor.allValues[1] shouldBe expectedResultWithRelativeBearing
                    })
                })
            })
        })
    })
}) {
   init {
       listener(InstantExecutorListener())
   }
}
