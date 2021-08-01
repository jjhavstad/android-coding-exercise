package com.groundspeak.rove

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

class NetworkStateListener : ConnectivityManager.NetworkCallback() {
    fun interface OnStateChangeListener {
        fun sendNetworkStatus(connected: Boolean)
    }

    private val onStateChangeListeners = mutableListOf<OnStateChangeListener>()

    fun addOnStateChangeListener(onStateChangeListener: OnStateChangeListener) {
        onStateChangeListeners.add(onStateChangeListener)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onStateChangeListeners.forEach {
            it.sendNetworkStatus(true)
        }
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onStateChangeListeners.forEach {
            it.sendNetworkStatus(false)
        }
    }

    override fun onUnavailable() {
        super.onUnavailable()
        onStateChangeListeners.forEach {
            it.sendNetworkStatus(false)
        }
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
    }

    override fun onLinkPropertiesChanged(
        network: Network,
        linkProperties: LinkProperties
    ) {
        super.onLinkPropertiesChanged(network, linkProperties)
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
    }
}
