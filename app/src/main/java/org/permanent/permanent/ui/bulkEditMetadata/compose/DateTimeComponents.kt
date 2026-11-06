package org.permanent.permanent.ui.bulkEditMetadata.compose

import android.graphics.Color as AndroidColor
import android.os.Build
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.permanent.permanent.R
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeEntry
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeField
import org.permanent.permanent.ui.bulkEditMetadata.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

internal val regularFont = FontFamily(Font(R.font.usual_regular))
internal val mediumFont = FontFamily(Font(R.font.usual_medium))

@Composable
internal fun DateTimeBox(
    entry: DateTimeEntry,
    fields: List<DateTimeField>,
    separator: String,
    focusRequesters: Map<DateTimeField, FocusRequester>,
    iconRes: Int,
    iconDescription: String,
    showsFormat: Boolean,
    onFieldChanged: (DateTimeField, String) -> Unit,
    onFieldFocused: () -> Unit,
    onFieldLeft: (DateTimeField) -> Unit,
    onFormatClick: () -> Unit,
    onIconClick: () -> Unit,
    error: String?,
    enabled: Boolean = true,
    hint: String? = null,
) {
    val blue50 = colorResource(R.color.blue50)
    val blue100 = colorResource(R.color.blue100)
    val blue900 = colorResource(R.color.blue900)
    var isFocused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.hasFocus }
                .then(
                    if (isFocused) Modifier.shadow(24.dp, RoundedCornerShape(12.dp), spotColor = blue900.copy(alpha = 0.16f))
                    else Modifier
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, if (isFocused) blue100 else blue50, RoundedCornerShape(12.dp))
                .padding(16.dp)
                .alpha(if (enabled) 1f else 0.4f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fields.forEachIndexed { index, field ->
                    if (index > 0) SeparatorText(separator)
                    DigitField(
                        value = entry[field],
                        placeholder = stringResource(field.placeholderRes),
                        isLast = field.next == null,
                        enabled = enabled,
                        focusRequester = focusRequesters.getValue(field),
                        onValueChange = { text ->
                            onFieldChanged(field, text)
                            // Moving focus inside the field's own text change skips its focus-lost event.
                            if (text.length == field.width) field.next?.let {
                                scope.launch { focusRequesters[it]?.requestFocus() }
                            }
                        },
                        onFocusChange = { focused -> if (focused) onFieldFocused() else onFieldLeft(field) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // The date box keeps an invisible pill so its fields line up with the time fields.
            FormatPill(
                format = entry.format,
                onClick = onFormatClick,
                modifier = if (showsFormat) Modifier else Modifier.alpha(0f),
                enabled = showsFormat && enabled
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = iconDescription,
                tint = blue900,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onIconClick)
            )
        }
        (error ?: hint)?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = TextStyle(
                    fontSize = 12.sp, lineHeight = 16.sp, fontFamily = regularFont,
                    color = colorResource(if (error != null) R.color.error500 else R.color.blue400)
                )
            )
        }
    }
}

private val DateTimeField.placeholderRes: Int
    get() = when (this) {
        DateTimeField.YEAR -> R.string.date_time_placeholder_year
        DateTimeField.MONTH -> R.string.date_time_placeholder_month
        DateTimeField.DAY -> R.string.date_time_placeholder_day
        DateTimeField.HOUR -> R.string.date_time_placeholder_hour
        DateTimeField.MINUTE -> R.string.date_time_placeholder_minute
        DateTimeField.SECOND -> R.string.date_time_placeholder_second
    }

@Composable
private fun SeparatorText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 14.sp, lineHeight = 24.sp, fontFamily = regularFont,
            color = colorResource(R.color.blue100)
        )
    )
}

// Selects its digits on focus, so typing replaces them.
@Composable
private fun DigitField(
    value: String,
    placeholder: String,
    isLast: Boolean,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val blue25 = colorResource(R.color.blue25)
    val blue200 = colorResource(R.color.blue200)
    val blue900 = colorResource(R.color.blue900)
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }
    if (fieldValue.text != value) fieldValue = TextFieldValue(value, TextRange(value.length))

    LaunchedEffect(isFocused) {
        if (isFocused) fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
    }

    val textStyle = TextStyle(
        fontSize = 14.sp, lineHeight = 24.sp, fontFamily = regularFont,
        color = blue900, textAlign = TextAlign.Center
    )
    BasicTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            if (it.text != value) onValueChange(it.text)
        },
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isFocused) blue25 else Color.Transparent)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused != isFocused) {
                    isFocused = it.isFocused
                    onFocusChange(it.isFocused)
                }
            },
        singleLine = true,
        enabled = enabled,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (fieldValue.text.isEmpty()) Text(placeholder, style = textStyle.copy(color = blue200))
                inner()
            }
        }
    )
}

@Composable
private fun FormatPill(format: TimeFormat, onClick: () -> Unit, modifier: Modifier, enabled: Boolean) {
    val text = stringResource(
        when (format) {
            TimeFormat.AM -> R.string.date_time_format_am
            TimeFormat.PM -> R.string.date_time_format_pm
            TimeFormat.H24 -> R.string.date_time_format_24h
        }
    )
    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(colorResource(R.color.blue25))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 10.sp, fontFamily = regularFont, letterSpacing = 1.6.sp,
                color = colorResource(R.color.blue900)
            )
        )
    }
}

@Composable
internal fun SheetCircleButton(iconRes: Int, description: String, dark: Boolean, onClick: () -> Unit) {
    val blue900 = colorResource(R.color.blue900)
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(2.dp)
            .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.16f))
            .clip(CircleShape)
            .background(if (dark) blue900 else Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = if (dark) Color.White else blue900,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Native Material calendar in its own sheet: the check writes the date, the X discards.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickADateSheet(initialDate: LocalDate?, today: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    val selectable = remember(today) { PickableDates(today) }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.takeIf { selectable.contains(it) }?.toUtcMillis(),
        yearRange = MIN_PICKABLE_YEAR..today.year,
        selectableDates = selectable
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SheetCircleButton(R.drawable.ic_close_light_blue, stringResource(R.string.button_cancel), dark = false, onClick = onDismiss)
                Text(
                    text = stringResource(R.string.date_time_pick_a_date),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontFamily = mediumFont, color = blue900)
                )
                SheetCircleButton(R.drawable.ic_check_simple, stringResource(R.string.save), dark = true) {
                    pickerState.selectedDateMillis?.let { onPick(it.toUtcDate()) } ?: onDismiss()
                }
            }
            DatePicker(
                state = pickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    subheadContentColor = blue900,
                    navigationContentColor = blue900,
                    weekdayContentColor = blue400,
                    selectedDayContainerColor = blue900,
                    dayContentColor = blue900,
                    todayContentColor = blue900,
                    todayDateBorderColor = blue900,
                    selectedYearContainerColor = blue900,
                    yearContentColor = blue900,
                    currentYearContentColor = blue900
                )
            )
        }
    }
}

private const val MIN_PICKABLE_YEAR = 1900

// The Material3 DatePicker works in UTC-midnight millis.
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcDate(): LocalDate = java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
private class PickableDates(private val today: LocalDate) : SelectableDates {
    fun contains(date: LocalDate): Boolean = date.year >= MIN_PICKABLE_YEAR && !date.isAfter(today)

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = contains(utcTimeMillis.toUtcDate())

    override fun isSelectableYear(year: Int): Boolean = year in MIN_PICKABLE_YEAR..today.year
}

// Three native wheels; every turn writes all three fields. The hour follows the AM/PM/24H pill.
@Composable
internal fun TimeWheelCard(entry: DateTimeEntry, onTimePicked: (Int, Int, Int) -> Unit, modifier: Modifier = Modifier) {
    val is24h = entry.format == TimeFormat.H24
    val hourRange = if (is24h) 0..23 else 1..12
    val hour = entry.hour.toIntOrNull()?.takeIf { it in hourRange } ?: if (is24h) 0 else 12
    val minute = entry.minute.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val second = entry.second.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val textColor = colorResource(R.color.blue900).toArgb()
    val pickers = remember { arrayOfNulls<NumberPicker>(3) }

    fun emit() {
        val (h, m, s) = pickers.map { it?.value ?: 0 }
        onTimePicked(h, m, s)
    }

    Box(
        modifier = modifier
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.16f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .wrapContentHeight()
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colorResource(R.color.blue50))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(hourRange to hour, 0..59 to minute, 0..59 to second).forEachIndexed { index, (range, start) ->
                AndroidView(
                    modifier = Modifier.width(56.dp),
                    factory = { context ->
                        NumberPicker(context).apply {
                            setFormatter { String.format(Locale.US, "%02d", it) }
                            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                setTextColor(textColor)
                                selectionDividerHeight = 0
                            }
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                            setOnValueChangedListener { _, _, _ -> emit() }
                            pickers[index] = this
                        }
                    },
                    update = { picker ->
                        if (picker.minValue != range.first || picker.maxValue != range.last) {
                            picker.minValue = range.first
                            picker.maxValue = range.last
                        }
                        if (picker.value != start) picker.value = start
                    }
                )
            }
        }
    }
}
