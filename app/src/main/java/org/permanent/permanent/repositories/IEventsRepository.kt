package org.permanent.permanent.repositories

import EventsPayload
import org.permanent.permanent.models.EventAction

interface IEventsRepository {
    fun sendEventAction(eventAction: EventAction, accountId: Int, data: Map<String, String>)
    fun sendEventAction(eventAction: EventAction, accountId: Int, entityId: String?, data: Map<String, String>)
    fun sendEvent(requestBody: EventsPayload)
}