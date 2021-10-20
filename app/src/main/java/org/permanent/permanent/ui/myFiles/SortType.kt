package org.permanent.permanent.ui.myFiles

enum class SortType(private val backendString: String, private val uiString: String) {
    NAME_ASCENDING("sort.alphabetical_asc", "Name (A-Z)"),
    NAME_DESCENDING("sort.alphabetical_desc", "Name (Z-A)"),
    DATE_ASCENDING("sort.display_date_asc", "Date (Oldest)"),
    DATE_DESCENDING("sort.display_date_desc", "Date (Newest)"),
    FILE_TYPE_ASCENDING("sort.type_asc", "File Type ↑"),
    FILE_TYPE_DESCENDING("sort.type_desc", "File Type ↓");

    fun toBackendString(): String = backendString
    fun toUIString(): String = uiString
}