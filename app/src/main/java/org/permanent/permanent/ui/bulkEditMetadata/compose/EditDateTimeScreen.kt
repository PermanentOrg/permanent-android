package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditDateTimeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDateTimeScreen(
    viewModel: EditDateTimeViewModel,
    cancel: () -> Unit) {

    val superLightBlue = colorResource(id = R.color.blue25)
    val blue50 = colorResource(R.color.blue50)
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    val regularFont = FontFamily(Font(R.font.usual_regular))
    val mediumFont = FontFamily(Font(R.font.usual_medium))

    val openAlertDialog = remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.initialDateMilis,
        selectableDates = PastOrPresentSelectableDates
    )

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = viewModel.initialHour,
        initialMinute = viewModel.initialMinute,
        is24Hour = true,
    )

    var selectedTime by remember { mutableStateOf(
        convertTimeToString(timePickerState.hour, timePickerState.minute, 0)
    )}

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(superLightBlue)
                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_primary),
                contentDescription = null,
                tint = blue900,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(id = R.string.add_date_time),
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = mediumFont,
                    color = blue900
                )
            )

            IconButton(onClick = cancel) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_light_blue),
                    contentDescription = stringResource(R.string.button_cancel),
                    tint = blue400
                )
            }
        }

        DatePicker(
            state = datePickerState,
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

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = blue50)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = mediumFont,
                    color = blue900
                )
            )

            Text(
                text = selectedTime,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = regularFont,
                    color = blue900
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(superLightBlue)
                    .clickable {
                        showTimePicker = true
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(modifier = Modifier
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = superLightBlue),
                onClick = {
                    cancel()
                }) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    fontSize = 14.sp,
                    color = blue900,
                    fontFamily = mediumFont,
                )
            }

            Button(modifier = Modifier
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue900),
                enabled = selectedDate.isNotEmpty(),
                onClick = {
                    openAlertDialog.value = true
                }) {
                if (viewModel.isBusy.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.add_date),
                        fontSize = 14.sp,
                        fontFamily = mediumFont,
                    )
                }
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onConfirm = {
                    showTimePicker = false
                    selectedTime = convertTimeToString(timePickerState.hour, timePickerState.minute, 0)
                }
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = superLightBlue,
                        timeSelectorUnselectedContainerColor = superLightBlue,
                        timeSelectorSelectedContainerColor = blue900,
                        timeSelectorSelectedContentColor = Color.White,
                        selectorColor = blue900,
                        periodSelectorSelectedContainerColor = blue900,
                        periodSelectorSelectedContentColor = Color.White
                    )
                )
            }
        }

        when {
            openAlertDialog.value -> {
                CustomDialog(
                    title = stringResource(id = R.string.date_confirmation_title),
                    subtitle = pluralStringResource(R.plurals.date_confirmation_message, viewModel.recordsCount, viewModel.recordsCount),
                    okButtonText = stringResource(id = R.string.set_date_and_time),
                    cancelButtonText = stringResource(id = R.string.button_cancel),
                    onConfirm = {
                        openAlertDialog.value = false
                        viewModel.updateDate(dateString = "$selectedDate $selectedTime")
                    }) {
                    openAlertDialog.value = false
                }
            }
        }
    }

    LaunchedEffect(key1 = viewModel.shouldClose.value, block = {
        if (viewModel.shouldClose.value) {
            cancel()
        }
    })
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.button_cancel), color = colorResource(R.color.blue900))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(id = R.string.save), color = colorResource(R.color.blue900))
            }
        },
        text = { content() }
    )
}

fun convertMillisToDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

fun convertTimeToString(hour: Int, min: Int, sec: Int): String =
    "%02d:%02d:%02d".format(Locale.US, hour, min, sec)

@OptIn(ExperimentalMaterial3Api::class)
private object PastOrPresentSelectableDates: SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= System.currentTimeMillis()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}