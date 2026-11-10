package org.permanent.permanent.edtf

import org.permanent.permanent.ui.BACKEND_DATE_TIME_FORMATTER
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

// One date, with no qualifier, no X digit and no gap: the only shape Stage 1 shows and edits.
val EdtfDateParts.isLevel0: Boolean
    get() = !uncertain && !approximate && year.length == 4 && (day.isEmpty() || month.isNotEmpty())

fun EdtfConverter.decodeLevel0(edtf: String): EdtfDateParts? =
    ((decode(edtf) as? EdtfDecodeResult.Success)?.value as? EdtfValue.Single)?.parts?.takeIf { it.isLevel0 }

// A range of two Level 0 dates opens on its start, as on Web and iOS.
fun EdtfConverter.decodeLevel0Start(edtf: String): EdtfDateParts? {
    val range = (decode(edtf) as? EdtfDecodeResult.Success)?.value as? EdtfValue.Range
        ?: return decodeLevel0(edtf)
    val start = (range.start as? EdtfSide.Dated)?.parts ?: return null
    val end = (range.end as? EdtfSide.Dated)?.parts ?: return null
    return start.takeIf { it.isLevel0 && end.isLevel0 }
}

// "Z" marks a UTC moment (old Web editor); an offset only records where the time was written.
private val UTC_START = Regex("""^[^/]*T[\d:.]+Z""")

fun String.isUtcTime(): Boolean = UTC_START.containsMatchIn(this)

// The clock as written; only a UTC moment is moved to device time (iOS).
fun EdtfDateParts.localDateTime(isUtc: Boolean): LocalDateTime? {
    val time = time ?: return null
    val written = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), time.hour, time.minute, time.second)
    return if (isUtc) written.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    else written
}

// A Level 0 displayTime as shown: the clock (null without a time) and the offset it was written with.
class WrittenTime(val parts: EdtfDateParts, val clock: LocalDateTime?, val storedOffset: ZoneOffset?)

fun EdtfConverter.decodeWrittenTime(edtf: String, rangeStart: Boolean): WrittenTime? {
    val parts = (if (rangeStart) decodeLevel0Start(edtf) else decodeLevel0(edtf)) ?: return null
    val isUtc = edtf.isUtcTime()
    return WrittenTime(parts, parts.localDateTime(isUtc), parts.time?.offset?.takeUnless { isUtc })
}

// The app's shown-date shape: "yyyy", "yyyy-MM", "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss".
fun EdtfDateParts.toShownDate(isUtc: Boolean = false): String =
    localDateTime(isUtc)?.format(BACKEND_DATE_TIME_FORMATTER)
        ?: listOf(year, month, day).filter { it.isNotEmpty() }.joinToString("-")
