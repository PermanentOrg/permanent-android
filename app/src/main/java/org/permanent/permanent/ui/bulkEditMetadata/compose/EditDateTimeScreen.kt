package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditDateTimeViewModel
import org.permanent.permanent.viewmodels.EditFileNamesViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDateTimeScreen(
    viewModel: EditDateTimeViewModel,
    cancel: () -> Unit) {

    val context = LocalContext.current
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val blue400 = Color(ContextCompat.getColor(context, R.color.blue400))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    var showTimePicker by remember { mutableStateOf(false) }

    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    var selectedTime by remember { mutableStateOf(
        convertTimeToString(timePickerState.hour, timePickerState.minute)
    )}

    Column(
        horizontalAlignment = Alignment.End
    ) {
        BottomSheetHeader(
            painterResource(id = R.drawable.ic_date_time),
            screenTitle = stringResource(id = R.string.add_date_time)
        )

        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            title = null,
            headline = null,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = blue900,
                dayContentColor = Color.Black,
                todayContentColor = Color.Black,
                todayDateBorderColor = blue900,
                selectedYearContainerColor = blue900,
                yearContentColor = Color.Black,
                currentYearContentColor = Color.Black
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time",
                style = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000)
                )
            )

            Text(
                text = selectedTime,
                style = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),

                    ),
                modifier = Modifier
                    .clickable {
                        showTimePicker = true
                    }
                    .background(color = Color(0x1F767680), shape = RoundedCornerShape(size = 8.dp))
                    .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
            )
        }

        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = superLightBlue),
                onClick = {
                    cancel()
                }) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    fontSize = 14.sp,
                    color = blue900,
                    fontFamily = regularFont,
                )
            }

            Button(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue900),
                onClick = {
                    viewModel.updateDate(selectedDate)
//                        openAlertDialog.value = true
                }) {
//                    if (viewModel.isBusy.value) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.width(32.dp),
//                            color = primaryColor,
//                            trackColor = superLightBlue,
//                        )
//                    } else {
                Text(
                    text = stringResource(R.string.add_date),
                    fontSize = 14.sp,
                    fontFamily = regularFont,
                )
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onConfirm = {
                    showTimePicker = false
                    selectedTime = convertTimeToString(timePickerState.hour, timePickerState.minute)
                }
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = blue900,
                        timeSelectorSelectedContentColor = Color.White,
                        selectorColor = blue900,
                        periodSelectorSelectedContainerColor = blue900,
                        periodSelectorSelectedContentColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() }
    )
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun convertTimeToString(hour: Int, min: Int): String {
    val amPM = if (hour < 12) {
        "AM"
    } else {
        "PM"
    }
    val minString = if (min < 10) {
        "0$min"
    } else {
        "$min"
    }

    return "$hour:$minString $amPM"
}

@Preview(showSystemUi = true)
@Composable
fun SimpleComposablePreview() {
    EditDateTimeScreen(cancel = {})
}