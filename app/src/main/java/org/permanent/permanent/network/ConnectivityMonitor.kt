package org.permanent.permanent.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Observable network reachability, the Android counterpart of iOS's ReachabilityManager
 * (VSP-1754). Drives the offline states of the file previews: classification of a load
 * failure (failed vs offline) and the auto-retry when connectivity returns.
 */
interface IConnectivityMonitor {
    /** Emits the current connectivity immediately on collect; distinct values only. */
    val isOnline: StateFlow<Boolean>

    val isConnected: Boolean get() = isOnline.value
}

/**
 * App-lifetime implementation backed by the default network callback, so no unregister is
 * needed. Uses NET_CAPABILITY_VALIDATED (not just availability) so captive portals and
 * dead wifi report as offline, matching what an actual image request would experience.
 */
class ConnectivityMonitorImpl(context: Context) : IConnectivityMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableStateFlow(queryCurrent())
    override val isOnline: StateFlow<Boolean> = _isOnline

    // Fresh snapshot: requests fail the instant a network dies, but the callback that
    // updates the flow can lag by seconds — classification must not trust the cache.
    override val isConnected: Boolean
        get() = queryCurrent()

    init {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _isOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }

            override fun onLost(network: Network) {
                // Another network may take over during a wifi -> cellular handover
                _isOnline.value = queryCurrent()
            }
        })
    }

    private fun queryCurrent(): Boolean =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
}

/**
 * Debug-only decorator that reports offline, optionally handing back to [real] after
 * [restoreAfterMs] — the equivalent of iOS's --forceOffline / --restoreConnectivityAfter=N
 * launch arguments, for exercising S7 and the reconnect auto-retry (S8) deterministically.
 */
class DebugForcedOfflineMonitor(
    scope: CoroutineScope,
    restoreAfterMs: Long,
    private val real: IConnectivityMonitor
) : IConnectivityMonitor {
    private val _isOnline = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = _isOnline

    init {
        if (restoreAfterMs > 0) {
            scope.launch {
                delay(restoreAfterMs)
                real.isOnline.collect { _isOnline.value = it }
            }
        }
    }
}
