package org.permanent.permanent

import android.content.Context
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.FeatureFlagsResponse
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// The server's switches: the last answer is loaded at start and refreshed once per process, so a
// cold start reads as the last run did until the new answer lands.
object ServerFeatureFlags {
    private const val EDTF_DATE = "edtf-date"

    @Volatile
    private var enabledNames: Set<String> = emptySet()
    private var prefs: PreferencesHelper? = null
    private var isRefreshed = false

    // On: screens show displayTime only, never the legacy displayDate (Web parity).
    val showsOnlyEdtfDate: Boolean
        get() = EDTF_DATE in enabledNames

    fun load(context: Context) {
        val prefs = PreferencesHelper(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        this.prefs = prefs
        enabledNames = prefs.getServerFeatureFlags()
    }

    // Main thread only. Runs on the first foreground, so background-only starts (uploads) skip it.
    fun refreshOnce() {
        if (isRefreshed) return
        isRefreshed = true
        NetworkClient.instance().getFeatureFlags().enqueue(object : Callback<FeatureFlagsResponse> {
            override fun onResponse(call: Call<FeatureFlagsResponse>, response: Response<FeatureFlagsResponse>) {
                val names = response.body()?.items?.mapNotNull { it.name }?.toSet() ?: return
                enabledNames = names
                prefs?.saveServerFeatureFlags(names)
            }

            override fun onFailure(call: Call<FeatureFlagsResponse>, t: Throwable) {}
        })
    }
}
