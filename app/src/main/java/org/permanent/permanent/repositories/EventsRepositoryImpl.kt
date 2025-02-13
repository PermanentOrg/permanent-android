package org.permanent.permanent.repositories

import EventsBodyPayload
import EventsPayload
import android.content.Context
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsRepositoryImpl(val context: Context) : IEventsRepository {
    override fun sendEventAction(
        eventAction: EventAction,
        accountId: Int,
        data: Map<String, String>
    ) {
        sendEventAction(eventAction, accountId, entityId = accountId.toString(), data)
    }

    override fun sendEventAction(
        eventAction: EventAction,
        accountId: Int,
        entityId: String?,
        data: Map<String, String>
    ) {
        val flavor = BuildConfig.FLAVOR
        var distinctId = accountId.toString()
        if(flavor == "staging") {
            distinctId = "$flavor:$distinctId"
        }
        val requestBody = EventsPayload(
            entity = eventAction.entity,
            action = eventAction.action,
            version = 1,
            entityId = entityId,
            body = EventsBodyPayload(
                event = eventAction.event,
                distinctId = distinctId,
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