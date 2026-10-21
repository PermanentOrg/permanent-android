package org.permanent.permanent.edtf

import java.time.ZoneOffset

data class EdtfTime(
    val hour: Int,
    val minute: Int = 0,
    val second: Int = 0,
    val offset: ZoneOffset? = null,
)

// Components stay strings so partial input survives: year "19" is 19XX, "0019" is 0019.
data class EdtfDateParts(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val time: EdtfTime? = null,
    val uncertain: Boolean = false,
    val approximate: Boolean = false,
) {
    val isBlank: Boolean
        get() = year.isBlank() && month.isBlank() && day.isBlank() && time == null
}

sealed interface EdtfSide {
    data class Dated(val parts: EdtfDateParts) : EdtfSide
    data object Open : EdtfSide
    data object Unknown : EdtfSide
}

sealed interface EdtfValue {
    data class Single(val parts: EdtfDateParts) : EdtfValue
    data object UnknownDate : EdtfValue
    data class Range(val start: EdtfSide, val end: EdtfSide) : EdtfValue
}

sealed interface EdtfEncodeResult {
    data class Success(val edtf: String) : EdtfEncodeResult
    data object Empty : EdtfEncodeResult
    data class Invalid(val errors: List<EdtfError>) : EdtfEncodeResult
}

sealed interface EdtfDecodeResult {
    data class Success(val value: EdtfValue) : EdtfDecodeResult
    data class Unsupported(val raw: String, val reason: Reason) : EdtfDecodeResult

    enum class Reason { INVALID, OUT_OF_SCOPE }
}

data class EdtfError(val side: Side, val code: Code) {
    enum class Side { START, END, RANGE }

    enum class Code {
        YEAR_FORMAT,
        MONTH_FORMAT,
        DAY_FORMAT,
        MONTH_RANGE,
        DAY_RANGE,
        INVALID_DAY_FOR_MONTH,
        HOUR_RANGE,
        MINUTE_RANGE,
        SECOND_RANGE,
        OFFSET_RANGE,
        TIME_WITHOUT_COMPLETE_DATE,
        TIME_WITH_QUALIFIER,
        RANGE_ORDER,
        NOT_ACCEPTED,
    }
}
