package org.permanent.permanent.ui

import android.content.Context
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.EdtfDateParts
import org.permanent.permanent.edtf.EdtfDecodeResult
import org.permanent.permanent.edtf.EdtfSide
import org.permanent.permanent.edtf.EdtfValue
import org.permanent.permanent.edtf.localDateTime
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.ui.bulkEditMetadata.zoneSuffix
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private const val RANGE_DASH = " — "
private const val BULLET = " • "

// The date under an item's name in lists and menu headers: "Jun. 24, 2026 • 12:56 AM CT",
// "c. 1985", "Before Nov. 12, 2025", "Sept. 24 — 30, 2001". "" without a date; a displayTime
// the app can't read is shown as stored (Web does the same).
fun DisplayDated.toListDate(context: Context = PermanentApplication.instance): String {
    val edtf = displayTime
    if (edtf.isNullOrEmpty()) return shownLegacyDate?.utcToLocalDateTime()?.let(::legacySide)?.text().orEmpty()
    val value = (EdtfConverter.decode(edtf) as? EdtfDecodeResult.Success)?.value ?: return edtf
    val texts = edtf.split('/')
    fun side(parts: EdtfDateParts, text: String) = listSide(parts, text.endsWith('Z'), timezone)

    return when (value) {
        EdtfValue.UnknownDate -> context.getString(R.string.date_time_unknown)
        is EdtfValue.Single -> side(value.parts, edtf)?.text() ?: edtf
        is EdtfValue.Range -> {
            val start = (value.start as? EdtfSide.Dated)?.let { side(it.parts, texts[0]) ?: return edtf }
            val end = (value.end as? EdtfSide.Dated)?.let { side(it.parts, texts[1]) ?: return edtf }
            when {
                start != null && end != null -> rangeText(start, end)
                end != null -> context.getString(R.string.date_time_before, end.text())
                start != null -> context.getString(R.string.date_time_after, start.text())
                else -> context.getString(R.string.date_time_unknown)
            }
        }
    }
}

// year keeps its X digits ("198X"); the clock is as shown, so it also gives the day of a UTC time.
private class ListSide(
    val year: String,
    val month: Int?,
    val day: Int?,
    val clock: LocalDateTime?,
    val zone: String?,
    private val qualifiers: EdtfDateParts? = null,
) {
    val isQualified: Boolean
        get() = qualifiers?.run { approximate || uncertain } == true

    fun monthAndDay(): String = "${shortMonth(month)} $day"

    fun dateText(): String {
        val date = when {
            day != null -> "${monthAndDay()}, $year"
            month != null -> "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)} $year"
            else -> year
        }
        return qualifiers?.qualified(date) ?: date
    }

    fun clockText(withZone: Boolean = true): String? =
        clock?.toClockText()?.withZone(zone.takeIf { withZone })

    fun text(): String = listOfNotNull(dateText(), clockText()).joinToString(BULLET)
}

// Null for a date with a gap ("1985-XX-03") or no year.
private fun listSide(parts: EdtfDateParts, isUtc: Boolean, timezone: String?): ListSide? {
    if (parts.year.isEmpty() || (parts.day.isNotEmpty() && parts.month.isEmpty())) return null
    val clock = if (parts.year.length == 4) parts.localDateTime(isUtc) else null
    return ListSide(
        year = clock?.year?.let { "%04d".format(Locale.US, it) } ?: parts.year.padEnd(4, 'X'),
        month = clock?.monthValue ?: parts.month.toIntOrNull(),
        day = clock?.dayOfMonth ?: parts.day.toIntOrNull(),
        clock = clock,
        zone = clock?.let { zoneSuffix(it, parts.time?.offset?.takeUnless { isUtc }, timezone) },
        qualifiers = parts,
    )
}

// A legacy "yyyy-MM-dd HH:mm:ss" in device time; it has no zone to name.
private fun legacySide(shown: String): ListSide? {
    val dateTime = shown.toLocalDateTimeOrNull() ?: return null
    return ListSide(
        year = "%04d".format(Locale.US, dateTime.year),
        month = dateTime.monthValue,
        day = dateTime.dayOfMonth,
        clock = dateTime.takeIf { shown.length > 10 },
        zone = null,
    )
}

private fun rangeText(start: ListSide, end: ListSide): String {
    val haveDays = start.day != null && end.day != null
    val sameYear = start.year == end.year
    val sameMonth = sameYear && start.month == end.month
    return when {
        haveDays && sameMonth && start.day == end.day -> {
            val clocks = when {
                start.clock != null && end.clock != null ->
                    start.clockText(withZone = false) + RANGE_DASH + end.clock.toClockText().withZone(end.zone ?: start.zone)
                else -> start.clockText() ?: end.clockText()
            }
            listOfNotNull(start.dateText(), clocks).joinToString(BULLET)
        }
        // The short forms hold only two plain days.
        !haveDays || !sameYear || start.isQualified || end.isQualified || start.clock != null || end.clock != null ->
            start.text() + RANGE_DASH + end.text()
        sameMonth -> "${start.monthAndDay()}$RANGE_DASH${end.day}, ${start.year}"
        else -> "${start.monthAndDay()}$RANGE_DASH${end.monthAndDay()}, ${start.year}"
    }
}

// "Jun.", "Sept.", "May".
private fun shortMonth(month: Int?): String {
    val value = Month.of(month ?: return "")
    return when {
        value == Month.SEPTEMBER -> "Sept."
        value == Month.MAY -> "May"
        else -> value.getDisplayName(TextStyle.SHORT, Locale.US) + "."
    }
}
