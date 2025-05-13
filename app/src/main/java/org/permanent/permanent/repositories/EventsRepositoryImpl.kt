package org.permanent.permanent.repositories

import EventsBodyPayload
import EventsPayload
import android.content.Context
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ChecklistResponse
import org.permanent.permanent.network.models.IChecklistListener
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

    override fun getCheckList(listener: IChecklistListener) {
        NetworkClient.instance().getCheckList().enqueue(object : Callback<ChecklistResponse> {

            override fun onResponse(call: Call<ChecklistResponse>, response: Response<ChecklistResponse>) {
                if (response.isSuccessful) {
                    val checklistItems = response.body()?.checklistItems
                    if (checklistItems != null) {
                        listener.onSuccess(checklistItems)
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(response.errorBody().toString())
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<ChecklistResponse>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}