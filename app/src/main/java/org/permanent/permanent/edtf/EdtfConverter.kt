package org.permanent.permanent.edtf

import io.github.openhistoricalmap.edtf.Edtf
import io.github.openhistoricalmap.edtf.EdtfTemporal
import io.github.openhistoricalmap.edtf.EdtfType
import io.github.openhistoricalmap.edtf.ParseOptions
import io.github.openhistoricalmap.edtf.types.EdtfInterval
import io.github.openhistoricalmap.edtf.types.Endpoint
import org.permanent.permanent.edtf.EdtfDecodeResult.Reason
import org.permanent.permanent.edtf.EdtfError.Code
import org.permanent.permanent.edtf.EdtfError.Side
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.EnumSet
import java.util.Locale

// Structured date fields <-> EDTF `displayTime`, in the vocabulary Web writes. edtf-java is the
// grammar gate only: its encoder normalizes times to UTC and drops qualifiers on masked dates.
object EdtfConverter {

    private const val OPEN = ".."
    private const val UNKNOWN_DATE = "XXXX-XX-XX"
    private const val MIN_OFFSET_SECONDS = -12 * 3600
    private const val MAX_OFFSET_SECONDS = 14 * 3600

    private val DATE_OR_INTERVAL =
        ParseOptions.DEFAULT.withAllowedTypes(EnumSet.of(EdtfType.DATE, EdtfType.INTERVAL))
    // "xxx" writes "+00:00" for UTC; Web never writes "Z".
    private val OFFSET_FORMATTER = DateTimeFormatter.ofPattern("xxx", Locale.US)

    private const val TIME_PATTERN = """T(\d{2}):(\d{2})(?::(\d{2})(?:\.\d{1,9})?)?"""
    private const val ZONE_PATTERN = """(Z|[+-]\d{2}(?::\d{2})?)"""
    private const val SIDE_PATTERN = """(\d{4}|\d{3}X|\d{2}XX|\dXXX|XXXX)(?:-(\d{2}|XX)(?:-(\d{2}|XX))?)?""" +
        """(?:$TIME_PATTERN$ZONE_PATTERN?)?([?~%])?"""
    private val SIDE = Regex(SIDE_PATTERN)
    private val SHAPE = Regex("""$SIDE_PATTERN|(?:$SIDE_PATTERN|\.\.)?/(?:$SIDE_PATTERN|\.\.)?""")
    private val TIME_ZONE = Regex("""($TIME_PATTERN)$ZONE_PATTERN""")

    // Seasons (YYYY-21…) and qualified all-X dates are valid EDTF the model can't hold.
    private val SEASON_LIKE = Regex("""(?:^|/)\d{4}-(?:1[3-9]|[2-9]\d)[?~%]?(?:/|$)""")
    private val QUALIFIED_ALL_X = Regex("""(?:^|/)XXXX(?:-XX){0,2}[?~%](?:/|$)""")
    private val QUALIFIER_AT_SIDE_END = Regex("""[?~%](?=/|$)""")
    private val ONE_OR_TWO_DIGITS = Regex("""\d{1,2}""")
    private val UP_TO_FOUR_DIGITS = Regex("""\d{1,4}""")

    fun deviceOffsetNow(): ZoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())

    fun encode(value: EdtfValue, defaultOffset: ZoneOffset = deviceOffsetNow()): EdtfEncodeResult {
        val gateSide = if (value is EdtfValue.Range) Side.RANGE else Side.START
        return try {
            val errors = mutableListOf<EdtfError>()
            fun encodeParts(parts: EdtfDateParts, side: Side): String {
                val trimmed = parts.copy(year = parts.year.trim(), month = parts.month.trim(), day = parts.day.trim())
                validate(trimmed, defaultOffset).mapTo(errors) { EdtfError(side, it) }
                return format(trimmed, defaultOffset)
            }
            fun encodeRangeSide(side: EdtfSide, label: Side): String = when (side) {
                EdtfSide.Open -> OPEN
                EdtfSide.Unknown -> ""
                is EdtfSide.Dated -> if (side.parts.isBlank) OPEN else encodeParts(side.parts, label)
            }

            val edtf = when (value) {
                EdtfValue.UnknownDate -> UNKNOWN_DATE
                is EdtfValue.Single ->
                    if (value.parts.isBlank) return EdtfEncodeResult.Empty else encodeParts(value.parts, Side.START)
                is EdtfValue.Range ->
                    encodeRangeSide(value.start, Side.START) + "/" + encodeRangeSide(value.end, Side.END)
            }
            if (errors.isEmpty()) gateError(edtf, gateSide)?.let(errors::add)
            if (errors.isEmpty()) EdtfEncodeResult.Success(edtf) else EdtfEncodeResult.Invalid(errors)
        } catch (e: RuntimeException) {
            EdtfEncodeResult.Invalid(listOf(EdtfError(gateSide, Code.NOT_ACCEPTED)))
        }
    }

    fun decode(edtf: String): EdtfDecodeResult {
        if (!SHAPE.matches(edtf) || SEASON_LIKE.containsMatchIn(edtf) || QUALIFIED_ALL_X.containsMatchIn(edtf)) {
            return EdtfDecodeResult.Unsupported(edtf, classify(edtf))
        }
        val value = try {
            decodeValue(edtf)
        } catch (e: RuntimeException) {
            null
        }
        return value?.let { EdtfDecodeResult.Success(it) }
            ?: EdtfDecodeResult.Unsupported(edtf, Reason.INVALID)
    }

    // Shared by encode and decode. Decode passes no default, so a zone-less time isn't range-checked.
    private fun validate(parts: EdtfDateParts, defaultOffset: ZoneOffset?): List<Code> = buildList {
        val (year, month, day) = parts
        if (year.isNotEmpty() && !year.matches(UP_TO_FOUR_DIGITS)) add(Code.YEAR_FORMAT)
        fieldError(month, 1..12, Code.MONTH_FORMAT, Code.MONTH_RANGE)?.let(::add)
        fieldError(day, 1..31, Code.DAY_FORMAT, Code.DAY_RANGE)?.let(::add)
        if (isEmpty() && month.isNotEmpty() && day.isNotEmpty() && !isRealDay(year, month.toInt(), day.toInt())) {
            add(Code.INVALID_DAY_FOR_MONTH)
        }
        val time = parts.time ?: return@buildList
        if (parts.uncertain || parts.approximate) add(Code.TIME_WITH_QUALIFIER)
        if (!year.isFullYear || month.isEmpty() || day.isEmpty()) add(Code.TIME_WITHOUT_COMPLETE_DATE)
        if (time.hour !in 0..23) add(Code.HOUR_RANGE)
        if (time.minute !in 0..59) add(Code.MINUTE_RANGE)
        if (time.second !in 0..59) add(Code.SECOND_RANGE)
        val offset = time.offset ?: defaultOffset ?: return@buildList
        if (offset.totalSeconds !in MIN_OFFSET_SECONDS..MAX_OFFSET_SECONDS || offset.totalSeconds % 60 != 0) {
            add(Code.OFFSET_RANGE)
        }
    }

    private fun format(parts: EdtfDateParts, defaultOffset: ZoneOffset): String = buildString {
        val (year, month, day, time) = parts
        append(year.padEnd(4, 'X'))
        if (month.isNotEmpty() || day.isNotEmpty()) append('-').append(month.ifEmpty { "XX" }.padStart(2, '0'))
        if (day.isNotEmpty()) append('-').append(day.padStart(2, '0'))
        if (time != null) {
            append(String.format(Locale.US, "T%02d:%02d:%02d", time.hour, time.minute, time.second))
            append(OFFSET_FORMATTER.format(time.offset ?: defaultOffset))
        }
        append(
            when {
                parts.uncertain && parts.approximate -> "%"
                parts.approximate -> "~"
                parts.uncertain -> "?"
                else -> ""
            }
        )
    }

    private fun fieldError(value: String, range: IntRange, format: Code, outOfRange: Code): Code? = when {
        value.isEmpty() -> null
        !value.matches(ONE_OR_TWO_DIGITS) -> format
        value.toInt() !in range -> outOfRange
        else -> null
    }

    private val String.isFullYear: Boolean
        get() = length == 4 && all(Char::isDigit)

    // An incomplete year is checked against 2000 (a leap year), as Web does.
    private fun isRealDay(year: String, month: Int, day: Int): Boolean = try {
        LocalDate.of(if (year.isFullYear) year.toInt() else 2000, month, day)
        true
    } catch (e: DateTimeException) {
        false
    }

    private fun gateError(edtf: String, side: Side): EdtfError? {
        val parsed = parse(edtf) ?: return EdtfError(side, Code.NOT_ACCEPTED)
        return if (isReversed(edtf, parsed)) EdtfError(Side.RANGE, Code.RANGE_ORDER) else null
    }

    private fun decodeValue(edtf: String): EdtfValue? {
        val value = if ('/' !in edtf) {
            val parts = decodeParts(edtf) ?: return null
            if (parts.isBlank) EdtfValue.UnknownDate else EdtfValue.Single(parts)
        } else {
            val (start, end) = edtf.split('/')
            EdtfValue.Range(decodeRangeSide(start) ?: return null, decodeRangeSide(end) ?: return null)
        }
        val parsed = parse(edtf) ?: return null
        return value.takeUnless { isReversed(edtf, parsed) }
    }

    private fun decodeRangeSide(text: String): EdtfSide? = when (text) {
        "" -> EdtfSide.Unknown
        OPEN -> EdtfSide.Open
        else -> decodeParts(text)?.let { if (it.isBlank) EdtfSide.Unknown else EdtfSide.Dated(it) }
    }

    private fun decodeParts(text: String): EdtfDateParts? {
        val match = SIDE.matchEntire(text) ?: return null
        val (year, month, day, hour, minute, second, zone, qualifier) = match.destructured
        val parts = EdtfDateParts(
            year = year.trimEnd('X'),
            month = month.takeUnless { it == "XX" }.orEmpty(),
            day = day.takeUnless { it == "XX" }.orEmpty(),
            time = hour.ifEmpty { null }?.let {
                EdtfTime(it.toInt(), minute.toInt(), second.ifEmpty { "0" }.toInt(), zone.ifEmpty { null }?.let(ZoneOffset::of))
            },
            uncertain = qualifier == "?" || qualifier == "%",
            approximate = qualifier == "~" || qualifier == "%",
        )
        return parts.takeIf { validate(it, defaultOffset = null).isEmpty() }
    }

    private fun classify(raw: String): Reason =
        if (parse(raw, ParseOptions.DEFAULT) != null) Reason.OUT_OF_SCOPE else Reason.INVALID

    // edtf-java can't parse a trailing qualifier next to X digits, so those are checked without it.
    private fun parse(edtf: String, options: ParseOptions = DATE_OR_INTERVAL): EdtfTemporal? {
        val text = if ('X' in edtf) edtf.replace(QUALIFIER_AT_SIDE_END, "") else edtf
        return try {
            Edtf.parse(text, options)
        } catch (e: RuntimeException) {
            null
        }
    }

    // The backend orders range ends by the time as written, ignoring offsets.
    private fun isReversed(edtf: String, parsed: EdtfTemporal): Boolean {
        val local = if (TIME_ZONE.containsMatchIn(edtf)) parse(edtf.replace(TIME_ZONE, "$1")) else parsed
        if (local !is EdtfInterval) return false
        val lower = local.lower() as? Endpoint.Bounded ?: return false
        val upper = local.upper() as? Endpoint.Bounded ?: return false
        return lower.value().min() > upper.value().max()
    }
}
