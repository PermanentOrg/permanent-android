package org.permanent.permanent.edtf

import org.permanent.permanent.ui.BACKEND_DATE_TIME_FORMATTER
import java.time.LocalDateTime
import java.time.ZoneId

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

// A zoned time as the wall clock in [zone]; a zone-less time as written.
fun EdtfDateParts.localDateTime(zone: ZoneId): LocalDateTime? {
    val time = time ?: return null
    val written = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), time.hour, time.minute, time.second)
    return time.offset?.let { written.atOffset(it).atZoneSameInstant(zone).toLocalDateTime() } ?: written
}

// The app's shown-date shape: "yyyy", "yyyy-MM", "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss".
fun EdtfDateParts.toShownDate(zone: ZoneId): String =
    localDateTime(zone)?.format(BACKEND_DATE_TIME_FORMATTER)
        ?: listOf(year, month, day).filter { it.isNotEmpty() }.joinToString("-")
