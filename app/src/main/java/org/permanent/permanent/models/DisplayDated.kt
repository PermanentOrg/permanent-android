package org.permanent.permanent.models

import org.permanent.permanent.ServerFeatureFlags
import org.permanent.permanent.ui.edtfToDisplayDate
import org.permanent.permanent.ui.utcToLocalDateTime

// A date from the date sheet. A null displayTime clears; a clear leaves the legacy value as it is.
data class EditedDate(val displayTime: String?, val legacyUtc: String?)

interface DisplayDated {
    // Legacy UTC value only: V1 bodies re-send it as displayDT, so it never holds EDTF or local time.
    var displayDate: String?
    var displayTime: String?

    // The legacy value as screens may show it: never while edtf-date is on (Web parity).
    val shownLegacyDate: String?
        get() = displayDate?.takeUnless { ServerFeatureFlags.showsOnlyEdtfDate }

    val shownDate: String?
        get() = displayTime?.edtfToDisplayDate() ?: shownLegacyDate?.utcToLocalDateTime()

    fun setEditedDate(date: EditedDate) {
        displayTime = date.displayTime
        date.legacyUtc?.let { displayDate = it }
    }
}

data class DatedValue(override var displayDate: String?, override var displayTime: String?) : DisplayDated

// Applies the date and says whether a failed V2 save may retry on V1. V1 writes only displayDT,
// which no screen shows once an item has a displayTime, so it is decided before the edit.
fun Collection<DisplayDated>.applyEditedDate(date: EditedDate): Boolean {
    val showsV1Date = !ServerFeatureFlags.showsOnlyEdtfDate && all { it.displayTime.isNullOrEmpty() }
    forEach { it.setEditedDate(date) }
    return showsV1Date
}
