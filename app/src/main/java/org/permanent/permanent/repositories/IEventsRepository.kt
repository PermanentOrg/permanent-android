package org.permanent.permanent.repositories

import EventsPayload
import org.permanent.permanent.models.EventAction

interface IEventsRepository {
    fun sendAccountEvent(eventAction: EventAction, accountId: Int, data: Map<String, String>)
    fun sendEvent(requestBody: EventsPayload)
}