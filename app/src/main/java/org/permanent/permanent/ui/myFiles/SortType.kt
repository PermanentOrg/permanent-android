package org.permanent.permanent.ui.myFiles

import android.content.Context
import androidx.annotation.StringRes
import org.permanent.permanent.R
import org.permanent.permanent.models.Record

enum class SortType(
    private val backendString: String,
    private val stelaString: String,
    private val uiString: String
) {
    NAME_ASCENDING("sort.alphabetical_asc", "alphabetical-ascending", "Name (A-Z)"),
    NAME_DESCENDING("sort.alphabetical_desc", "alphabetical-descending", "Name (Z-A)"),
    DATE_ASCENDING("sort.display_date_asc", "date-ascending", "Date (Oldest)"),
    DATE_DESCENDING("sort.display_date_desc", "date-descending", "Date (Newest)"),
    FILE_TYPE_ASCENDING("sort.type_asc", "type-ascending", "File Type ↑"),
    FILE_TYPE_DESCENDING("sort.type_desc", "type-descending", "File Type ↓");

    fun toBackendString(): String = backendString
    fun toStelaString(): String = stelaString
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

    val field: SortField
        get() = when (this) {
            NAME_ASCENDING, NAME_DESCENDING -> SortField.NAME
            DATE_ASCENDING, DATE_DESCENDING -> SortField.DATE
            FILE_TYPE_ASCENDING, FILE_TYPE_DESCENDING -> SortField.TYPE
        }

    // The direction as the sort popup lists it.
    @get:StringRes
    val directionRes: Int
        get() = when (this) {
            NAME_ASCENDING -> R.string.sort_name_ascending
            NAME_DESCENDING -> R.string.sort_name_descending
            DATE_ASCENDING -> R.string.sort_date_ascending
            DATE_DESCENDING -> R.string.sort_date_descending
            FILE_TYPE_ASCENDING -> R.string.sort_type_ascending
            FILE_TYPE_DESCENDING -> R.string.sort_type_descending
        }

    // "Name  •  A → Z", as the sort row shows it.
    fun toLabel(context: Context): String {
        val direction = when (this) {
            NAME_ASCENDING -> R.string.sort_name_ascending_short
            NAME_DESCENDING -> R.string.sort_name_descending_short
            else -> directionRes
        }
        return context.getString(
            R.string.sort_label, context.getString(field.nameRes), context.getString(direction)
        )
    }

    companion object {
        // Accepts both the V1 and the Stela spelling.
        fun fromServerValue(value: String?): SortType? =
            values().firstOrNull { it.backendString == value || it.stelaString == value }
    }
}

enum class SortField(@StringRes val nameRes: Int) {
    NAME(R.string.my_files_sort_criteria_name),
    DATE(R.string.my_files_sort_criteria_date),
    TYPE(R.string.sort_field_type);

    // Picking a field in the sort popup applies this direction.
    val defaultSort: SortType
        get() = when (this) {
            NAME -> SortType.NAME_ASCENDING
            DATE -> SortType.DATE_DESCENDING
            TYPE -> SortType.FILE_TYPE_ASCENDING
        }

    // Listed in the popup in this order.
    val sorts: List<SortType>
        get() = when (this) {
            NAME -> listOf(SortType.NAME_ASCENDING, SortType.NAME_DESCENDING)
            DATE -> listOf(SortType.DATE_DESCENDING, SortType.DATE_ASCENDING)
            TYPE -> listOf(SortType.FILE_TYPE_ASCENDING, SortType.FILE_TYPE_DESCENDING)
        }
}
