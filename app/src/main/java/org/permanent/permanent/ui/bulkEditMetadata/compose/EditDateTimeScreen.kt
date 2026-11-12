package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import org.permanent.permanent.R
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeEntryError
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeField
import org.permanent.permanent.viewmodels.EditDateTimeViewModel

private val DATE_FIELDS = listOf(DateTimeField.YEAR, DateTimeField.MONTH, DateTimeField.DAY)
private val TIME_FIELDS = listOf(DateTimeField.HOUR, DateTimeField.MINUTE, DateTimeField.SECOND)

@Composable
fun EditDateTimeScreen(
    viewModel: EditDateTimeViewModel,
    cancel: () -> Unit) {

    val blue25 = colorResource(R.color.blue25)
    val blue50 = colorResource(R.color.blue50)
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    val error500 = colorResource(R.color.error500)
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { DateTimeField.entries.associateWith { FocusRequester() } }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimeWheel by remember { mutableStateOf(false) }

    val entry = viewModel.entry.value
    val errors = entry.errors
    val dateError = errors.firstOrNull { it.isDateError }?.let { stringResource(it.messageRes) }
    val timeError = errors.firstOrNull { !it.isDateError }?.let { stringResource(it.messageRes) }

    var sheetTop by remember { mutableFloatStateOf(0f) }
    var timeBoxTop by remember { mutableFloatStateOf(0f) }

    if (viewModel.showZonePicker.value) {
        TimeZonePicker(viewModel)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .onGloballyPositioned { sheetTop = it.positionInRoot().y }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                .background(Color.White)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus(); showTimeWheel = false } }
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (viewModel.isAdjusting) R.string.adjust_date_and_time else R.string.add_date_time
                        ),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.16).sp,
                            fontFamily = mediumFont, color = blue900
                        )
                    )
                    viewModel.preview?.let {
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = regularFont, color = blue400)
                        )
                    }
                }
                SheetCircleButton(R.drawable.ic_close_light_blue, stringResource(R.string.button_cancel), dark = false, onClick = cancel)
            }

            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    DateTimeBox(
                        entry = entry,
                        fields = DATE_FIELDS,
                        separator = "/",
                        focusRequesters = focusRequesters,
                        iconRes = R.drawable.ic_calendar_primary,
                        iconDescription = stringResource(R.string.date_time_pick_a_date),
                        showsFormat = false,
                        onFieldChanged = viewModel::onFieldChanged,
                        onFieldFocused = { showTimeWheel = false },
                        onFieldLeft = viewModel::onFieldLeft,
                        onFormatClick = {},
                        onIconClick = {
                            focusManager.clearFocus()
                            showTimeWheel = false
                            showDatePicker = true
                        },
                        error = dateError
                    )
                    Box(modifier = Modifier.onGloballyPositioned { timeBoxTop = it.positionInRoot().y }) {
                        DateTimeBox(
                            entry = entry,
                            fields = TIME_FIELDS,
                            separator = ":",
                            focusRequesters = focusRequesters,
                            iconRes = R.drawable.ic_clock_primary,
                            iconDescription = stringResource(R.string.set_date_and_time),
                            showsFormat = true,
                            onFieldChanged = viewModel::onFieldChanged,
                            onFieldFocused = { showTimeWheel = false },
                            onFieldLeft = viewModel::onFieldLeft,
                            onFormatClick = viewModel::onFormatClick,
                            onIconClick = {
                                focusManager.clearFocus()
                                showTimeWheel = !showTimeWheel
                            },
                            error = timeError,
                            enabled = entry.usesTime,
                            hint = stringResource(R.string.date_time_hint_time_needs_full_date).takeUnless { entry.usesTime }
                        )
                    }
                }

                // Hidden where the save can't store a zone (V1 only, e.g. Shared With Me).
                if (viewModel.savesZone) {
                    TimeZoneRow(
                        zone = entry.zone,
                        offsetLabel = entry.zone?.let(viewModel::zoneOffsetLabel),
                        enabled = viewModel.canPickZone,
                        onClick = {
                            focusManager.clearFocus()
                            showTimeWheel = false
                            viewModel.onZoneRowClick()
                        }
                    )
                }

                if (viewModel.canClear) {
                    HorizontalDivider(color = blue50)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                focusManager.clearFocus()
                                showTimeWheel = false
                                viewModel.onClearClick()
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_broom_error),
                            contentDescription = null,
                            tint = error500,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.date_time_clear),
                            style = TextStyle(fontSize = 14.sp, lineHeight = 24.sp, fontFamily = regularFont, color = error500)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue25),
                    onClick = cancel
                ) {
                    Text(
                        text = stringResource(R.string.button_cancel),
                        fontSize = 14.sp,
                        color = blue900,
                        fontFamily = mediumFont,
                    )
                }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue900),
                    enabled = viewModel.canSave,
                    onClick = {
                        focusManager.clearFocus()
                        showTimeWheel = false
                        viewModel.onSaveClick()
                    }
                ) {
                    if (viewModel.isBusy.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(if (viewModel.isAdjusting) R.string.save else R.string.add_date),
                            fontSize = 14.sp,
                            fontFamily = mediumFont,
                        )
                    }
                }
            }
        }

        // Drawn over the sheet, not in a Popup: a Popup window inside the dialog is re-placed wrongly on recomposition.
        if (showTimeWheel && entry.usesTime) {
            TimeWheelCard(
                entry = entry,
                onTimePicked = viewModel::onTimePicked,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 24.dp)
                    .offset { IntOffset(0, (timeBoxTop - sheetTop).roundToInt()) }
            )
        }
    }

    if (showDatePicker) {
        PickADateSheet(
            initialDate = entry.fullDate(),
            today = viewModel.today,
            onDismiss = { showDatePicker = false },
            onPick = {
                showDatePicker = false
                viewModel.onDatePicked(it)
            }
        )
    }

    if (viewModel.showConfirmation.value) {
        CustomDialog(
            title = stringResource(id = R.string.date_confirmation_title),
            subtitle = pluralStringResource(R.plurals.date_confirmation_message, viewModel.recordsCount, viewModel.recordsCount),
            okButtonText = stringResource(id = R.string.set_date_and_time),
            cancelButtonText = stringResource(id = R.string.button_cancel),
            onConfirm = viewModel::save,
            onDismiss = viewModel::onConfirmationDismissed
        )
    }

    LaunchedEffect(key1 = viewModel.shouldClose.value, block = {
        if (viewModel.shouldClose.value) {
            cancel()
        }
    })
}

private val DateTimeEntryError.isDateError: Boolean
    get() = this == DateTimeEntryError.MISSING_YEAR || this == DateTimeEntryError.MISSING_MONTH

private val DateTimeEntryError.messageRes: Int
    get() = when (this) {
        DateTimeEntryError.MISSING_YEAR -> R.string.date_time_error_missing_year
        DateTimeEntryError.MISSING_MONTH -> R.string.date_time_error_missing_month
        DateTimeEntryError.MISSING_HOUR -> R.string.date_time_error_missing_hour
        DateTimeEntryError.HOUR_RANGE -> R.string.date_time_error_hour_range
    }
