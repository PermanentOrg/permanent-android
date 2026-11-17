package org.permanent.permanent.ui.fileView

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.ShownSide
import org.permanent.permanent.edtf.decodeShown
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.ui.bulkEditMetadata.zoneSuffix
import org.permanent.permanent.ui.toInfoRowDate
import org.permanent.permanent.ui.withZone

// What File Info's Date row shows: the words after its label, and one line or a From and a To line.
class InfoRowDate(val labelDetail: String?, val firstLine: Line, val secondLine: Line? = null) {
    class Line(val caption: String?, val text: String)

    companion object {
        // Null when the item has no date to show, so the row shows its placeholder.
        fun from(dated: DisplayDated, context: Context): InfoRowDate? {
            val shown = dated.displayTime?.let(EdtfConverter::decodeShown)
                ?: return (dated.displayTime ?: dated.shownDate)?.let { InfoRowDate(null, Line(null, it.toInfoRowDate())) }

            val written = listOfNotNull(shown.start, shown.end).filterIsInstance<ShownSide.Written>()
            // The words go on the label only when every written side carries the same ones.
            val sharesQualifiers = written.map { it.parts.approximate to it.parts.uncertain }.toSet().size == 1
            val labelDetail = written.firstOrNull()?.takeIf { sharesQualifiers }?.let { side ->
                listOfNotNull(
                    context.getString(R.string.date_time_approximate).takeIf { side.parts.approximate },
                    context.getString(R.string.date_time_uncertain).takeIf { side.parts.uncertain },
                ).joinToString(" • ").ifEmpty { null }
            }

            fun text(side: ShownSide?): String = when (side) {
                is ShownSide.Written -> {
                    val date = side.shownDate.toInfoRowDate().withZone(side.zoneSuffix(dated.timezone))
                    side.parts.qualifierSymbol?.takeUnless { sharesQualifiers }?.let { "$it $date" } ?: date
                }
                else -> context.getString(R.string.date_time_unknown)
            }

            val start = shown.start
            val end = shown.end
            return when {
                start is ShownSide.Written && end == null -> InfoRowDate(labelDetail, Line(null, text(start)))
                start is ShownSide.Empty && end is ShownSide.Written ->
                    InfoRowDate(labelDetail, Line(null, context.getString(R.string.date_time_before, text(end))))
                start is ShownSide.Written && end is ShownSide.Empty ->
                    InfoRowDate(labelDetail, Line(null, context.getString(R.string.date_time_after, text(start))))
                written.isNotEmpty() -> InfoRowDate(
                    labelDetail,
                    Line(context.getString(R.string.date_time_range_from), text(start)),
                    Line(context.getString(R.string.date_time_range_to), text(end)),
                )
                else -> InfoRowDate(labelDetail, Line(null, context.getString(R.string.date_time_unknown_date_and_time)))
            }
        }
    }
}
