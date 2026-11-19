package org.permanent.permanent.models

import org.permanent.permanent.ServerFeatureFlags
import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.decodeShown
import org.permanent.permanent.ui.utcToLocalDateTime

sealed interface ZoneChange {
    data object Keep : ZoneChange
    data class Set(val id: String) : ZoneChange
    data object Clear : ZoneChange
}

// A date from the date sheet. A null displayTime clears; a clear leaves the legacy value as it is.
data class EditedDate(
    val displayTime: String?,
    val legacyUtc: String?,
    val zone: ZoneChange = ZoneChange.Keep,
    // V1 holds one plain moment: never a qualifier, an unknown date or a range.
    val allowsV1Retry: Boolean = true,
)

interface DisplayDated {
    // Legacy UTC value only: V1 bodies re-send it as displayDT, so it never holds EDTF or local time.
    var displayDate: String?
    var displayTime: String?
    // IANA id from location.timezone; stored on the record, not in its Location row.
    var timezone: String?

    // The legacy value as screens may show it: never while edtf-date is on (Web parity).
    val shownLegacyDate: String?
        get() = displayDate?.takeUnless { ServerFeatureFlags.showsOnlyEdtfDate }

    // The first written side of displayTime (null when it holds no date), a displayTime the app
    // can't read verbatim, else the legacy date.
    val shownDate: String?
        get() {
            val edtf = displayTime ?: return shownLegacyDate?.utcToLocalDateTime()
            val shown = EdtfConverter.decodeShown(edtf) ?: return edtf
            return shown.firstWritten?.shownDate
        }

    // True also for an unknown date, which shows no date in lists.
    val hasDate: Boolean
        get() = !displayTime.isNullOrEmpty() || shownDate != null

    // shownDate for a file name: never a displayTime the app can't read.
    val fileNameDate: String?
        get() = shownDate?.takeUnless { it == displayTime && EdtfConverter.decodeShown(it) == null }

    fun setEditedDate(date: EditedDate) {
        setDateOnly(date)
        applyZone(date.zone)
    }

    fun setDateOnly(date: EditedDate) {
        displayTime = date.displayTime
        date.legacyUtc?.let { displayDate = it }
    }

    fun applyZone(change: ZoneChange) {
        when (change) {
            ZoneChange.Keep -> Unit
            is ZoneChange.Set -> timezone = change.id
            ZoneChange.Clear -> timezone = null
        }
    }
}

data class DatedValue(
    override var displayDate: String?,
    override var displayTime: String?,
    override var timezone: String?,
) : DisplayDated

// Applies the date (not the zone: save bodies read the zone the item had) and says whether a
// failed V2 save may retry on V1. V1 writes only displayDT, which no screen shows once an item
// has a displayTime, so it is decided before the edit.
fun Collection<DisplayDated>.applyEditedDate(date: EditedDate): Boolean {
    val showsV1Date = !ServerFeatureFlags.showsOnlyEdtfDate && all { it.displayTime.isNullOrEmpty() }
    forEach { it.setDateOnly(date) }
    return showsV1Date && date.allowsV1Retry
}
