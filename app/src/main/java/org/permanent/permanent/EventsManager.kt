package org.permanent.permanent

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

class EventsManager(context: Context) {
    private val mp: MixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN, true)

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

    fun trackPageView(page: EventPage) {
        val properties = JSONObject()
        properties.put("page", page.value)
        mp.track("Screen View", properties)
    }

    fun resetUser() {
        mp.reset()
    }
}

enum class EventType(val value: String) {
    SignUp("Sign up"),
    SignIn("Sign in"),
    PageView("Screen View"),
    InitiateUpload("Initiate Upload"),
    FinalizeUpload("Finalize Upload"),
    EditArchiveProfile("Edit Archive Profile"),
    PurchaseStorage("Purchase Storage")
}

enum class EventPage(val value: String) {
    AccountMenu("Account Menu"),
    ArchiveMenu("Archive Menu"),
    ArchiveProfile("Archive Profile"),
    Storage("Storage"),
    RedeemGift("RedeemGift")
}