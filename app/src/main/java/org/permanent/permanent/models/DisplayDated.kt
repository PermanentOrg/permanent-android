package org.permanent.permanent.models

import org.permanent.permanent.ui.edtfToDisplayDate
import org.permanent.permanent.ui.localToUtcDateTime
import org.permanent.permanent.ui.toEdtfLevel0
import org.permanent.permanent.ui.utcToLocalDateTime

interface DisplayDated {
    // Legacy UTC value only: V1 bodies re-send it as displayDT, so it never holds EDTF or local time.
    var displayDate: String?
    var displayTime: String?

    val shownDate: String?
        get() = displayTime?.edtfToDisplayDate() ?: displayDate?.utcToLocalDateTime()

    // An editor's "yyyy-MM-dd" or local "yyyy-MM-dd HH:mm:ss": V1 writes the first, V2 the second.
    fun setEditedDate(value: String) {
        displayDate = value.localToUtcDateTime()
        displayTime = value.toEdtfLevel0()
    }
}
