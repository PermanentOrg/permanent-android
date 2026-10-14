package org.permanent.permanent.models

import org.permanent.permanent.ui.edtfToDisplayDate
import org.permanent.permanent.ui.toEdtfLevel0

interface DisplayDated {
    // Legacy value only: V1 bodies re-send it as displayDT, so it never holds EDTF.
    var displayDate: String?
    var displayTime: String?

    val shownDate: String?
        get() = displayTime?.edtfToDisplayDate() ?: displayDate

    // An editor's "yyyy-MM-dd" or local "yyyy-MM-dd HH:mm:ss": V1 writes the first, V2 the second.
    fun setEditedDate(value: String) {
        displayDate = value
        displayTime = value.toEdtfLevel0()
    }
}
