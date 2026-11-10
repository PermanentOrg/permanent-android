package org.permanent.permanent.ui.bulkEditMetadata

import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.EdtfDateParts
import org.permanent.permanent.edtf.EdtfEncodeResult
import org.permanent.permanent.edtf.EdtfTime
import org.permanent.permanent.edtf.EdtfValue
import org.permanent.permanent.edtf.decodeWrittenTime
import org.permanent.permanent.edtf.toShownDate
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.models.EditedDate
import org.permanent.permanent.models.ZoneChange
import org.permanent.permanent.ui.BACKEND_DATE_TIME_FORMATTER
import org.permanent.permanent.ui.toLocalDateTimeOrNull
import org.permanent.permanent.ui.utcToLocalDateTime
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset

enum class TimeFormat {
    AM, PM, H24;

    fun next(): TimeFormat = entries[(ordinal + 1) % entries.size]
}

enum class DateTimeField(val width: Int) {
    YEAR(4), MONTH(2), DAY(2), HOUR(2), MINUTE(2), SECOND(2);

    // Where typing moves when this field fills up; day and second keep the focus.
    val next: DateTimeField?
        get() = when (this) {
            YEAR -> MONTH
            MONTH -> DAY
            HOUR -> MINUTE
            MINUTE -> SECOND
            DAY, SECOND -> null
        }
}

enum class DateTimeEntryError { MISSING_YEAR, MISSING_MONTH, MISSING_HOUR, HOUR_RANGE }

// The date sheet's typed fields. Digits are kept as typed: a format switch never converts them,
// and a short year stays short on screen (it is padded only in the saved string).
data class DateTimeEntry(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val hour: String = "",
    val minute: String = "",
    val second: String = "",
    val format: TimeFormat = TimeFormat.AM,
    val zoneId: String? = null,
    // The offset the record's string carried: kept in a repeated hour, shown when there is no zone.
    val storedOffset: ZoneOffset? = null,
) {
    operator fun get(field: DateTimeField): String = when (field) {
        DateTimeField.YEAR -> year
        DateTimeField.MONTH -> month
        DateTimeField.DAY -> day
        DateTimeField.HOUR -> hour
        DateTimeField.MINUTE -> minute
        DateTimeField.SECOND -> second
    }

    fun with(field: DateTimeField, text: String): DateTimeEntry {
        val digits = text.filter { it in '0'..'9' }.take(field.width)
        return when (field) {
            DateTimeField.YEAR -> copy(year = digits)
            DateTimeField.MONTH -> copy(month = digits)
            DateTimeField.DAY -> copy(day = digits)
            DateTimeField.HOUR -> copy(hour = digits)
            DateTimeField.MINUTE -> copy(minute = digits)
            DateTimeField.SECOND -> copy(second = digits)
        }
    }

    val isEmpty: Boolean
        get() = DateTimeField.entries.all { this[it].isEmpty() }

    // A time needs a full date (EDTF). With a partial date the time fields are kept but not used.
    val usesTime: Boolean
        get() = year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty()

    private val hasTime: Boolean
        get() = hour.isNotEmpty() || minute.isNotEmpty() || second.isNotEmpty()

    // A zone goes only with a time (iOS).
    val canPickZone: Boolean
        get() = usesTime && hasTime

    val zone: TimeZoneOption?
        get() = zoneId?.let(TimeZoneCatalog::option)

    fun bounds(field: DateTimeField, today: LocalDate = LocalDate.now()): IntRange = when (field) {
        DateTimeField.YEAR -> 1..today.year
        DateTimeField.MONTH -> 1..12
        DateTimeField.DAY -> 1..daysInMonth()
        DateTimeField.HOUR -> if (format == TimeFormat.H24) 0..23 else 1..12
        DateTimeField.MINUTE, DateTimeField.SECOND -> 0..59
    }

    // Runs when a field fills up or loses focus: an out-of-range value moves to the nearest bound.
    // A year or month change re-checks the day.
    fun clamp(field: DateTimeField, today: LocalDate = LocalDate.now()): DateTimeEntry {
        val value = this[field].toIntOrNull() ?: return this
        val clamped = value.coerceIn(bounds(field, today))
        val text = if (field == DateTimeField.YEAR && clamped == value) this[field]
        else clamped.toString().padStart(if (field == DateTimeField.YEAR) 1 else 2, '0')
        val entry = with(field, text)
        return if (field == DateTimeField.YEAR || field == DateTimeField.MONTH) entry.clamp(DateTimeField.DAY, today)
        else entry
    }

    // A short or missing year is checked against 2000, a leap year, so 29 February stays.
    private fun daysInMonth(): Int {
        val month = month.toIntOrNull()?.takeIf { it in 1..12 } ?: return 31
        val year = year.toIntOrNull()?.takeIf { this.year.length == 4 && it >= 1 } ?: 2000
        return YearMonth.of(year, month).lengthOfMonth()
    }

    val errors: List<DateTimeEntryError>
        get() = buildList {
            if (year.isEmpty() && (month.isNotEmpty() || day.isNotEmpty())) add(DateTimeEntryError.MISSING_YEAR)
            if (month.isEmpty() && day.isNotEmpty()) add(DateTimeEntryError.MISSING_MONTH)
            if (usesTime && hasTime) {
                if (hour.isEmpty()) add(DateTimeEntryError.MISSING_HOUR)
                else if (hour.toInt() !in bounds(DateTimeField.HOUR)) add(DateTimeEntryError.HOUR_RANGE)
            }
        }

    private val hour24: Int?
        get() {
            val typed = hour.toIntOrNull() ?: return null
            return when (format) {
                TimeFormat.AM -> if (typed == 12) 0 else typed
                TimeFormat.PM -> if (typed < 12) typed + 12 else typed
                TimeFormat.H24 -> typed
            }
        }

    private val parts: EdtfDateParts
        get() = EdtfDateParts(
            year = year.padStart(4, '0'),
            month = month.padStart(2, '0').takeIf { month.isNotEmpty() }.orEmpty(),
            day = day.padStart(2, '0').takeIf { day.isNotEmpty() }.orEmpty(),
            time = hour24?.takeIf { usesTime }?.let { EdtfTime(it, minute.toIntOrNull() ?: 0, second.toIntOrNull() ?: 0) },
        )

    // The wall clock the fields describe; a missing month or day is 1, a missing time is midnight (iOS).
    // Null while a field is still being typed out of range (a lone "0" is clamped only when it is left).
    fun wallClock(): LocalDateTime? {
        if (errors.isNotEmpty() || year.isEmpty()) return null
        val parts = parts
        val time = parts.time
        return try {
            LocalDateTime.of(
                parts.year.toInt(), parts.month.toIntOrNull() ?: 1, parts.day.toIntOrNull() ?: 1,
                time?.hour ?: 0, time?.minute ?: 0, time?.second ?: 0
            )
        } catch (e: DateTimeException) {
            null
        }
    }

    // The value to save, or null while the fields have an error. An empty entry clears.
    // A time carries the zone's offset at that clock time, or no offset without a zone (iOS).
    fun toEditedDate(openingZone: String?): EditedDate? {
        if (isEmpty) return EditedDate(displayTime = null, legacyUtc = null, zone = ZoneChange.Clear)
        val wallClock = wallClock() ?: return null
        val zone = zone?.takeIf { canPickZone }
        val offset = zone?.offsetAt(wallClock, storedOffset)
        val edtf = (EdtfConverter.encode(EdtfValue.Single(parts), offset) as? EdtfEncodeResult.Success)?.edtf
            ?: return null
        val legacyUtc = wallClock.atZone(zone?.zone ?: ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC).format(BACKEND_DATE_TIME_FORMATTER)
        val change = when {
            zone != null -> ZoneChange.Set(zone.id)
            zoneId == null && openingZone != null -> ZoneChange.Clear
            else -> ZoneChange.Keep
        }
        return EditedDate(edtf, legacyUtc, change)
    }

    // After the time in the preview: the chosen zone's short name; the saved string has no offset without one.
    val zoneSuffix: String?
        get() {
            if (!canPickZone) return null
            val clock = wallClock() ?: return null
            return zone?.shortName(clock, storedOffset)
        }

    // The shown-date shape the Info row formatter reads, or null while the fields don't make a date.
    val shownDate: String?
        get() = wallClock()?.let { parts.toShownDate() }

    // The typed date when all three date fields make a real day, for the calendar to open on.
    fun fullDate(): LocalDate? = try {
        if (year.length == 4 && month.isNotEmpty() && day.isNotEmpty()) LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        else null
    } catch (e: DateTimeException) {
        null
    }

    fun withDate(date: LocalDate): DateTimeEntry =
        copy(year = date.year.padded(4), month = date.monthValue.padded(2), day = date.dayOfMonth.padded(2))

    fun withTime(hour: Int, minute: Int, second: Int): DateTimeEntry =
        copy(hour = hour.padded(2), minute = minute.padded(2), second = second.padded(2))

    private fun Int.padded(width: Int): String = toString().padStart(width, '0')

    companion object {
        // A Level 0 displayTime (or a range of two, on its start) fills the fields with the clock as
        // written. Without a displayTime the legacy date is used where screens show it. No zone.
        fun from(dated: DisplayDated): DateTimeEntry {
            dated.displayTime?.let { displayTime ->
                val written = EdtfConverter.decodeWrittenTime(displayTime, rangeStart = true) ?: return DateTimeEntry()
                val clock = written.clock ?: return DateTimeEntry(written.parts.year, written.parts.month, written.parts.day)
                return fromClock(clock).copy(storedOffset = written.storedOffset)
            }
            val legacy = dated.shownLegacyDate?.utcToLocalDateTime()?.toLocalDateTimeOrNull() ?: return DateTimeEntry()
            return fromClock(legacy)
        }

        // A time opens in AM/PM, never 24H (iOS).
        private fun fromClock(clock: LocalDateTime): DateTimeEntry {
            val hour12 = (clock.hour % 12).let { if (it == 0) 12 else it }
            return DateTimeEntry(format = if (clock.hour < 12) TimeFormat.AM else TimeFormat.PM)
                .withDate(clock.toLocalDate())
                .withTime(hour12, clock.minute, clock.second)
        }
    }
}
