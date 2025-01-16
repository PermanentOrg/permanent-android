import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventsBodyPayload(
    private val analytics: EventsAnalyticPayload
) : Parcelable {
    val event: String
        get() = analytics.event
    val distinctId: String
        get() = analytics.distinctId
    val data: Map<String, String>
        get() = analytics.data

    constructor(event: String, distinctId: String, data: Map<String, String>) : this(
        EventsAnalyticPayload(
            event,
            distinctId,
            data
        )
    )

    @Parcelize
    data class EventsAnalyticPayload(
        val event: String,
        val distinctId: String,
        val data: Map<String, String>
    ) : Parcelable
}

@Parcelize
data class EventsPayload(
    val entity: String,
    val action: String,
    val version: Int,
    val entityId: String?,
    val body: EventsBodyPayload
) : Parcelable