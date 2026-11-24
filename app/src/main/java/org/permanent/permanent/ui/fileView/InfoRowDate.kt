package org.permanent.permanent.ui.fileView

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.ShownSide
import org.permanent.permanent.edtf.decodeShown
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.ui.bulkEditMetadata.zoneSuffix
import org.permanent.permanent.ui.toInfoRowDate
import org.permanent.permanent.ui.toListDate
import org.permanent.permanent.ui.withZone

// What File Info's Date row shows: one line, or a From and a To line.
class InfoRowDate(val firstLine: Line, val secondLine: Line? = null) {
    class Line(val caption: String?, val text: String)

    companion object {
        // Null when the item has no date to show, so the row shows its placeholder.
        fun from(dated: DisplayDated, context: Context): InfoRowDate? {
            // A displayTime the row can't lay out gets the list text ("198X ?", or as stored).
            val shown = dated.displayTime?.takeIf { it.isNotEmpty() }?.let(EdtfConverter::decodeShown)
                ?: return dated.displayTime?.takeIf { it.isNotEmpty() }?.let { InfoRowDate(Line(null, dated.toListDate(context))) }
                    ?: dated.shownDate?.let { InfoRowDate(Line(null, it.toInfoRowDate())) }

            fun text(side: ShownSide?): String = when (side) {
                is ShownSide.Written ->
                    side.parts.qualified(side.shownDate.toInfoRowDate().withZone(side.zoneSuffix(dated.timezone)))
                else -> context.getString(R.string.date_time_unknown)
            }

            val start = shown.start
            val end = shown.end
            return when {
                start is ShownSide.Written && end == null -> InfoRowDate(Line(null, text(start)))
                start is ShownSide.Empty && end is ShownSide.Written ->
                    InfoRowDate(Line(null, context.getString(R.string.date_time_before, text(end))))
                start is ShownSide.Written && end is ShownSide.Empty ->
                    InfoRowDate(Line(null, context.getString(R.string.date_time_after, text(start))))
                start is ShownSide.Written || end is ShownSide.Written -> InfoRowDate(
                    Line(context.getString(R.string.date_time_range_from), text(start)),
                    Line(context.getString(R.string.date_time_range_to), text(end)),
                )
                else -> InfoRowDate(Line(null, context.getString(R.string.date_time_unknown_date_and_time)))
            }
        }
    }
}
