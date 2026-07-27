package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Record

enum class SortType(private val backendString: String, private val uiString: String) {
    NAME_ASCENDING("sort.alphabetical_asc", "Name (A-Z)"),
    NAME_DESCENDING("sort.alphabetical_desc", "Name (Z-A)"),
    DATE_ASCENDING("sort.display_date_asc", "Date (Oldest)"),
    DATE_DESCENDING("sort.display_date_desc", "Date (Newest)"),
    FILE_TYPE_ASCENDING("sort.type_asc", "File Type ↑"),
    FILE_TYPE_DESCENDING("sort.type_desc", "File Type ↓");

    fun toBackendString(): String = backendString
    fun toUIString(): String = uiString

    // Local equivalent of the backend sort options, for the Stela V2 children
    // endpoint which takes no sort param (VSP-1778). Date keys are the normalized
    // "yyyy-MM-dd[ HH:mm:ss]" strings on Record, so lexicographic order is
    // chronological; missing dates sort oldest. Type keys are the dotted backend
    // types ("type.folder.*" < "type.record.*"), so folders group before records.
    fun toComparator(): Comparator<Record> {
        val byName = compareBy(String.CASE_INSENSITIVE_ORDER) { record: Record ->
            record.displayName.orEmpty()
        }
        return when (this) {
            NAME_ASCENDING -> byName
            NAME_DESCENDING -> byName.reversed()
            DATE_ASCENDING -> compareBy<Record> { it.displayDate.orEmpty() }.then(byName)
            DATE_DESCENDING -> compareByDescending<Record> { it.displayDate.orEmpty() }.then(byName)
            FILE_TYPE_ASCENDING -> compareBy<Record> { it.backendType.orEmpty() }.then(byName)
            FILE_TYPE_DESCENDING -> compareByDescending<Record> { it.backendType.orEmpty() }.then(byName)
        }
    }
}