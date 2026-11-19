package org.permanent.permanent.edtf

import org.permanent.permanent.ui.BACKEND_DATE_TIME_FORMATTER
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

// A full year and no gap: the only date shape screens show and the sheet edits.
val EdtfDateParts.isPlainDate: Boolean
    get() = year.length == 4 && (day.isEmpty() || month.isNotEmpty())

sealed interface ShownSide {
    // "Z" marks a UTC moment (old Web editor); an offset only records where the time was written.
    class Written(val parts: EdtfDateParts, private val isUtc: Boolean) : ShownSide {
        val clock: LocalDateTime?
            get() = parts.localDateTime(isUtc)

        val storedOffset: ZoneOffset?
            get() = parts.time?.offset?.takeUnless { isUtc }

        // The app's shown-date shape: "yyyy", "yyyy-MM", "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss".
        val shownDate: String
            get() = parts.toShownDate(isUtc)
    }

    // ".." in a range.
    data object Empty : ShownSide

    data object Unknown : ShownSide
}

// A displayTime as screens read it: one side, or the two sides of a range.
class ShownValue(val start: ShownSide, val end: ShownSide?) {
    // Lists, headers and sort show this side.
    val firstWritten: ShownSide.Written?
        get() = start as? ShownSide.Written ?: end as? ShownSide.Written
}

// Null for a string the app can't show (X digits, seasons, invalid): callers show it verbatim.
fun EdtfConverter.decodeShown(edtf: String): ShownValue? {
    val value = (decode(edtf) as? EdtfDecodeResult.Success)?.value ?: return null
    val texts = edtf.split('/')
    fun written(parts: EdtfDateParts, text: String): ShownSide.Written? =
        if (parts.isPlainDate) ShownSide.Written(parts, text.endsWith('Z')) else null

    fun side(side: EdtfSide, text: String): ShownSide? = when (side) {
        EdtfSide.Open -> ShownSide.Empty
        EdtfSide.Unknown -> ShownSide.Unknown
        is EdtfSide.Dated -> written(side.parts, text)
    }
    return when (value) {
        EdtfValue.UnknownDate -> ShownValue(ShownSide.Unknown, null)
        is EdtfValue.Single -> written(value.parts, edtf)?.let { ShownValue(it, null) }
        is EdtfValue.Range -> ShownValue(side(value.start, texts[0]) ?: return null, side(value.end, texts[1]) ?: return null)
    }
}

// The clock as written; only a UTC moment is moved to device time (iOS).
fun EdtfDateParts.localDateTime(isUtc: Boolean): LocalDateTime? {
    val time = time ?: return null
    val written = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), time.hour, time.minute, time.second)
    return if (isUtc) written.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    else written
}

// The app's shown-date shape: "yyyy", "yyyy-MM", "yyyy-MM-dd" or "yyyy-MM-dd HH:mm:ss".
fun EdtfDateParts.toShownDate(isUtc: Boolean = false): String =
    localDateTime(isUtc)?.format(BACKEND_DATE_TIME_FORMATTER)
        ?: listOf(year, month, day).filter { it.isNotEmpty() }.joinToString("-")
