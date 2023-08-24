package org.permanent.permanent

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONException
import org.json.JSONObject

class EventsManager(context: Context) {
    private val mp: MixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_KEY, true)

    fun sendToMixpanel(event: EventType, properties: JSONObject? = null) {
        mp.track(event.value, properties)
    }

    fun setUserProfile(id: Int? = null, email: String? = null) {
        id?.let {
            mp.identify(it.toString())
        }
        email?.let {
            mp.people.set("email", email)
        }
    }

    fun resetUser() {
        mp.reset()
    }
}

enum class EventType(val value: String) {
    SignUp("Sign up"),
    SignIn("Sign in")
}