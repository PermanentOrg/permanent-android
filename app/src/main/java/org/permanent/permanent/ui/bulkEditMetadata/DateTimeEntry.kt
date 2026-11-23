package org.permanent.permanent.ui.bulkEditMetadata

import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.EdtfDateParts
import org.permanent.permanent.edtf.EdtfEncodeResult
import org.permanent.permanent.edtf.EdtfError
import org.permanent.permanent.edtf.EdtfTime
import org.permanent.permanent.edtf.EdtfSide
import org.permanent.permanent.edtf.EdtfValue
import org.permanent.permanent.edtf.ShownSide
import org.permanent.permanent.edtf.decodeShown
import org.permanent.permanent.edtf.toShownDate
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.models.EditedDate
import org.permanent.permanent.models.ZoneChange
import org.permanent.permanent.ui.BACKEND_DATE_TIME_FORMATTER
import org.permanent.permanent.ui.toInfoRowDate
import org.permanent.permanent.ui.toLocalDateTimeOrNull
import org.permanent.permanent.ui.utcToLocalDateTime
import org.permanent.permanent.ui.withZone
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

enum class DateQualifier { APPROXIMATE, UNCERTAIN, UNKNOWN }

enum class RangeSide { START, END }

enum class DateRangeError { END_BEFORE_START, NOT_ACCEPTED }

// One side of the date sheet: its typed fields and toggles. Digits are kept as typed: a format switch never converts them,
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
    val approximate: Boolean = false,
    val uncertain: Boolean = false,
    // An unknown side keeps no fields; what it held waits in DateRangeEntry.
    val unknown: Boolean = false,
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

    val hasFullDate: Boolean
        get() = year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty()

    // EDTF has no symbol for a time, so a qualified side is a date only.
    val hasQualifier: Boolean
        get() = approximate || uncertain

    // A time needs a full date (EDTF). With a partial date the time fields are kept but not used.
    val usesTime: Boolean
        get() = hasFullDate && !hasQualifier

    val hasTime: Boolean
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
            uncertain = uncertain,
            approximate = approximate,
        )

    // The fields with the zone's offset at this side's clock time; no offset without a zone (iOS).
    fun encodedParts(): EdtfDateParts? {
        val clock = wallClock() ?: return null
        val offset = zone?.takeIf { canPickZone }?.offsetAt(clock, storedOffset)
        return parts.let { it.copy(time = it.time?.copy(offset = offset)) }
    }

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

    // After the time in the preview: the chosen zone's short name; the saved string has no offset without one.
    val zoneSuffix: String?
        get() {
            if (!canPickZone) return null
            val clock = wallClock() ?: return null
            return zone?.shortName(clock, storedOffset)
        }

    // The shown-date shape the Info row formatter reads, or null while the fields don't make a date.
    val shownDate: String?
        get() = wallClock()?.let { clock ->
            if (parts.time != null) clock.format(BACKEND_DATE_TIME_FORMATTER) else parts.toShownDate()
        }

    // The side for the line under the title, with its qualifiers; null while the fields don't make a date.
    fun preview(shortMonth: Boolean, unknownText: String): String? {
        if (unknown) return unknownText
        val text = shownDate?.toInfoRowDate(shortMonth)?.withZone(zoneSuffix) ?: return null
        return parts.qualified(text)
    }

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
        fun from(side: ShownSide): DateTimeEntry = when (side) {
            ShownSide.Empty -> DateTimeEntry()
            ShownSide.Unknown -> DateTimeEntry(unknown = true)
            is ShownSide.Written -> {
                val parts = side.parts
                val fields = side.clock?.let { fromClock(it).copy(storedOffset = side.storedOffset) }
                    ?: DateTimeEntry(parts.year, parts.month, parts.day)
                fields.copy(approximate = parts.approximate, uncertain = parts.uncertain)
            }
        }

        // A time opens in AM/PM, never 24H (iOS).
        fun fromClock(clock: LocalDateTime): DateTimeEntry {
            val hour12 = (clock.hour % 12).let { if (it == 0) 12 else it }
            return DateTimeEntry(format = if (clock.hour < 12) TimeFormat.AM else TimeFormat.PM)
                .withDate(clock.toLocalDate())
                .withTime(hour12, clock.minute, clock.second)
        }
    }
}

// The date sheet: one date, or the two sides of a range, and what a side held before a toggle
// emptied it. Both sides share one time zone.
data class DateRangeEntry(
    val start: DateTimeEntry = DateTimeEntry(),
    // Null while "Use a date range" is off.
    val end: DateTimeEntry? = null,
    private val keptSides: Map<RangeSide, DateTimeEntry> = emptyMap(),
    private val keptClocks: Map<RangeSide, DateTimeEntry> = emptyMap(),
    private val keptEnd: DateTimeEntry? = null,
) {
    operator fun get(side: RangeSide): DateTimeEntry? = if (side == RangeSide.START) start else end

    private val sides: List<DateTimeEntry>
        get() = listOfNotNull(start, end)

    val isRange: Boolean
        get() = end != null

    // Nothing to save but a clear.
    val isEmpty: Boolean
        get() = sides.all { it.isEmpty && !it.unknown }

    private val holdsNoDate: Boolean
        get() = sides.all { it.isEmpty }

    // A record with a qualifier or a second side opens in the full view.
    val opensFull: Boolean
        get() = isRange || start.hasQualifier || start.unknown

    fun with(side: RangeSide, entry: DateTimeEntry): DateRangeEntry =
        if (side == RangeSide.START) copy(start = entry) else copy(end = entry)

    fun withZone(zoneId: String?): DateRangeEntry =
        copy(start = start.copy(zoneId = zoneId), end = end?.copy(zoneId = zoneId))

    // Unknown empties the side; Approximate and Uncertain empty its clock. Each comes back when
    // its toggle goes off.
    fun withQualifier(side: RangeSide, qualifier: DateQualifier, on: Boolean): DateRangeEntry {
        val entry = this[side] ?: return this
        if (qualifier == DateQualifier.UNKNOWN) {
            if (entry.unknown == on) return this
            val changed = if (on) DateTimeEntry(format = entry.format, unknown = true)
            else (keptSides[side] ?: DateTimeEntry(format = entry.format)).copy(unknown = false)
            val kept = if (on) keptSides + (side to entry) else keptSides - side
            return copy(keptSides = kept).with(side, changed.copy(zoneId = start.zoneId))
        }
        if (entry.unknown) return this
        var changed = if (qualifier == DateQualifier.APPROXIMATE) entry.copy(approximate = on) else entry.copy(uncertain = on)
        var clocks = keptClocks
        if (changed.hasQualifier && changed.hasTime) {
            clocks = clocks + (side to changed)
            changed = changed.copy(hour = "", minute = "", second = "")
        } else if (!changed.hasQualifier) {
            clocks[side]?.let { changed = changed.copy(hour = it.hour, minute = it.minute, second = it.second, format = it.format) }
            clocks = clocks - side
        }
        return copy(keptClocks = clocks).with(side, changed)
    }

    fun withRange(on: Boolean): DateRangeEntry = when {
        on == isRange -> this
        on -> copy(end = (keptEnd ?: DateTimeEntry()).copy(zoneId = start.zoneId), keptEnd = null)
        else -> copy(end = null, keptEnd = end)
    }

    // Empties everything: toggles and range off, no zone.
    fun cleared(): DateRangeEntry = DateRangeEntry(start = DateTimeEntry(format = start.format))

    // An emptied side saves as open; the shared zone stays.
    fun cleared(side: RangeSide): DateRangeEntry {
        val entry = this[side] ?: return this
        return copy(keptSides = keptSides - side, keptClocks = keptClocks - side)
            .with(side, DateTimeEntry(format = entry.format, zoneId = start.zoneId))
    }

    private fun encode(): EdtfEncodeResult? {
        fun rangeSide(entry: DateTimeEntry): EdtfSide? = when {
            entry.unknown -> EdtfSide.Unknown
            entry.isEmpty -> EdtfSide.Open
            else -> entry.encodedParts()?.let { EdtfSide.Dated(it) }
        }
        val value = when {
            end != null -> EdtfValue.Range(rangeSide(start) ?: return null, rangeSide(end) ?: return null)
            start.unknown -> EdtfValue.UnknownDate
            else -> EdtfValue.Single(start.encodedParts() ?: return null)
        }
        return EdtfConverter.encode(value, defaultOffset = null)
    }

    // Why the whole value can't be saved; a side's own errors show under its boxes instead.
    val error: DateRangeError?
        get() {
            if (isEmpty) return null
            val invalid = encode() as? EdtfEncodeResult.Invalid ?: return null
            return if (invalid.errors.any { it.code == EdtfError.Code.RANGE_ORDER }) DateRangeError.END_BEFORE_START
            else DateRangeError.NOT_ACCEPTED
        }

    // The value to save, or null while it can't be saved. An empty sheet clears.
    fun toEditedDate(openingZone: String?): EditedDate? {
        if (isEmpty) return EditedDate(displayTime = null, legacyUtc = null, zone = ZoneChange.Clear)
        val edtf = (encode() as? EdtfEncodeResult.Success)?.edtf ?: return null
        val zone = start.zone?.takeIf { sides.any { it.canPickZone } }
        val change = when {
            zone != null -> ZoneChange.Set(zone.id)
            holdsNoDate || (start.zoneId == null && openingZone != null) -> ZoneChange.Clear
            else -> ZoneChange.Keep
        }
        val isPlain = !opensFull
        val legacyUtc = start.wallClock()?.takeIf { isPlain }?.atZone(zone?.zone ?: ZoneId.systemDefault())
            ?.withZoneSameInstant(ZoneOffset.UTC)?.format(BACKEND_DATE_TIME_FORMATTER)
        return EditedDate(edtf, legacyUtc, change, allowsV1Retry = isPlain)
    }

    // The line under the title: a full month for one date, "Oct." on each side of a range.
    fun preview(unknownText: String): String? {
        val end = end ?: return start.preview(shortMonth = false, unknownText)
        if (isEmpty) return null
        val texts = listOf(start, end).map {
            if (it.isEmpty && !it.unknown) "" else it.preview(shortMonth = true, unknownText) ?: return null
        }
        return "${texts[0]} — ${texts[1]}".trim()
    }

    companion object {
        // Every shape displayTime can hold; not [full], its first written side as one plain date.
        // Without a displayTime the legacy date is used where screens show it. No zone.
        fun from(dated: DisplayDated, full: Boolean): DateRangeEntry {
            dated.displayTime?.let { displayTime ->
                val shown = EdtfConverter.decodeShown(displayTime) ?: return DateRangeEntry()
                if (full) return DateRangeEntry(DateTimeEntry.from(shown.start), shown.end?.let(DateTimeEntry::from))
                val first = shown.firstWritten ?: return DateRangeEntry()
                return DateRangeEntry(DateTimeEntry.from(first).copy(approximate = false, uncertain = false))
            }
            val legacy = dated.shownLegacyDate?.utcToLocalDateTime()?.toLocalDateTimeOrNull() ?: return DateRangeEntry()
            return DateRangeEntry(DateTimeEntry.fromClock(legacy))
        }
    }
}
