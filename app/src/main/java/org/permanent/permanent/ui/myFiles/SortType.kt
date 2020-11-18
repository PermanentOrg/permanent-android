package org.permanent.permanent.ui.myFiles

enum class SortType(val string: String) {
    NAME_ASCENDING("sort.alphabetical_asc"),
    NAME_DESCENDING("sort.alphabetical_desc"),
    DATE_ASCENDING("sort.display_date_asc"),
    DATE_DESCENDING("sort.display_date_desc"),
    FILE_TYPE_ASCENDING("sort.type_asc"),
    FILE_TYPE_DESCENDING("sort.type_desc");

    override fun toString(): String = string
}