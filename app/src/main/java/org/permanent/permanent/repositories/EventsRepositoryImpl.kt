package org.permanent.permanent.repositories

import EventsBodyPayload
import EventsPayload
import android.content.Context
import org.permanent.permanent.EventType
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.IEventsService
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsRepositoryImpl(val context: Context) : IEventsRepository {

    override fun sendAccountEvent(eventAction: EventAction, accountId: Int, data: Map<String, String>) {
        val requestBody = EventsPayload(
            entity = eventAction.entity,
            action = eventAction.action,
            version = 1,
            entityId = accountId.toString(),
            body = EventsBodyPayload(
                event = eventAction.event,
                distinctId = "staging:$accountId",
                data = data
            )
        )
        sendEvent(requestBody)
    }

    override fun sendEvent(requestBody: EventsPayload) {
        NetworkClient.instance().sendEvents(requestBody).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                print(t.message)
            }
        })
    }

}