package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import org.permanent.permanent.R
import org.permanent.permanent.ui.bulkEditMetadata.DateQualifier
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeEntryError
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeField
import org.permanent.permanent.ui.bulkEditMetadata.RangeSide
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
    val focusRequesters = remember {
        RangeSide.entries.associateWith { DateTimeField.entries.associateWith { FocusRequester() } }
    }
    var datePickerSide by remember { mutableStateOf<RangeSide?>(null) }
    var timeWheelSide by remember { mutableStateOf<RangeSide?>(null) }

    val range = viewModel.range.value
    val showsOptions = viewModel.showsOptions.value
    val leaveFields = {
        focusManager.clearFocus()
        timeWheelSide = null
    }

    var sheetTop by remember { mutableFloatStateOf(0f) }
    val timeBoxTops = remember { mutableStateMapOf<RangeSide, Float>() }

    if (viewModel.showZonePicker.value) {
        TimeZonePicker(viewModel)
        return
    }

    var buttonsHeight by remember { mutableIntStateOf(0) }
    // The part of the keyboard that rises above the button row.
    val keyboardOverFields = with(LocalDensity.current) {
        (WindowInsets.ime.getBottom(this) - WindowInsets.navigationBars.getBottom(this) - buttonsHeight).coerceAtLeast(0).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(SHEET_HEIGHT_FRACTION)
            .onGloballyPositioned { sheetTop = it.positionInRoot().y }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clip(SheetShape)
                .background(Color.White)
                .pointerInput(Unit) { detectTapGestures { leaveFields() } }
                .navigationBarsPadding()
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
                    viewModel.errorRes?.let {
                        Text(
                            text = stringResource(it),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = regularFont, color = error500)
                        )
                    }
                }
                SheetCircleButton(R.drawable.ic_close_light_blue, stringResource(R.string.button_cancel), dark = false, onClick = cancel)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    // Only the fields make room for the keyboard; the buttons stay at the bottom, under it.
                    .padding(bottom = keyboardOverFields)
                    // Without this the bottom sheet takes the drag and the content can't scroll back up.
                    .nestedScroll(rememberNestedScrollInteropConnection())
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showsOptions) {
                    OptionSwitchRow(stringResource(R.string.date_time_use_range), checked = range.isRange) {
                        leaveFields()
                        viewModel.onRangeToggle(it)
                    }
                }
                RangeSide.entries.filter { range[it] != null }.forEach { side ->
                    if (side == RangeSide.END) HorizontalDivider(color = blue50)
                    DateSide(
                        viewModel = viewModel,
                        side = side,
                        showsOptions = showsOptions,
                        focusRequesters = focusRequesters.getValue(side),
                        leaveFields = leaveFields,
                        onDateIconClick = { datePickerSide = side },
                        onTimeIconClick = { timeWheelSide = if (timeWheelSide == side) null else side },
                        onFieldFocused = { timeWheelSide = null },
                        onTimeBoxPlaced = { timeBoxTops[side] = it }
                    )
                }
                if (!showsOptions) HorizontalDivider(color = blue50)

                if (viewModel.canClear && !range.isRange) {
                    SheetActionRow(R.drawable.ic_broom_error, stringResource(R.string.date_time_clear), error500, regularFont) {
                        leaveFields()
                        viewModel.onClearClick()
                    }
                }
                if (viewModel.savesZone) {
                    SheetActionRow(
                        R.drawable.ic_sliders_primary,
                        stringResource(if (showsOptions) R.string.date_time_fewer_options else R.string.date_time_more_options),
                        blue900, mediumFont
                    ) {
                        leaveFields()
                        viewModel.onOptionsClick()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .onSizeChanged { buttonsHeight = it.height }
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
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
                        leaveFields()
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
        timeWheelSide?.takeIf { viewModel.entry(it).usesTime }?.let { side ->
            TimeWheelCard(
                entry = viewModel.entry(side),
                onTimePicked = { hour, minute, second -> viewModel.onTimePicked(side, hour, minute, second) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 24.dp)
                    .offset { IntOffset(0, ((timeBoxTops[side] ?: 0f) - sheetTop).roundToInt()) }
            )
        }
    }

    datePickerSide?.let { side ->
        PickADateSheet(
            initialDate = viewModel.entry(side).fullDate(),
            today = viewModel.today,
            onDismiss = { datePickerSide = null },
            onPick = {
                datePickerSide = null
                viewModel.onDatePicked(side, it)
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

// One side's label (in a range), date box, time box, zone row and, in the full view, its toggles.
@Composable
private fun DateSide(
    viewModel: EditDateTimeViewModel,
    side: RangeSide,
    showsOptions: Boolean,
    focusRequesters: Map<DateTimeField, FocusRequester>,
    leaveFields: () -> Unit,
    onDateIconClick: () -> Unit,
    onTimeIconClick: () -> Unit,
    onFieldFocused: () -> Unit,
    onTimeBoxPlaced: (Float) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val entry = viewModel.entry(side)
    val errors = entry.errors
    val dateError = errors.firstOrNull { it.isDateError }?.let { stringResource(it.messageRes) }
    val timeError = errors.firstOrNull { !it.isDateError }?.let { stringResource(it.messageRes) }

    if (viewModel.range.value.isRange) {
        RangeSideLabel(
            stringResource(if (side == RangeSide.START) R.string.date_time_range_from else R.string.date_time_range_to)
        ) {
            leaveFields()
            viewModel.onSideClearClick(side)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        DateTimeBox(
            entry = entry,
            fields = DATE_FIELDS,
            separator = "/",
            focusRequesters = focusRequesters,
            iconRes = R.drawable.ic_calendar_primary,
            iconDescription = stringResource(R.string.date_time_pick_a_date),
            showsFormat = false,
            onFieldChanged = { field, text -> viewModel.onFieldChanged(side, field, text) },
            onFieldFocused = onFieldFocused,
            onFieldLeft = { viewModel.onFieldLeft(side, it) },
            onFormatClick = {},
            onIconClick = {
                leaveFields()
                onDateIconClick()
            },
            error = dateError,
            enabled = !entry.unknown
        )
        Box(modifier = Modifier.onGloballyPositioned { onTimeBoxPlaced(it.positionInRoot().y) }) {
            DateTimeBox(
                entry = entry,
                fields = TIME_FIELDS,
                separator = ":",
                focusRequesters = focusRequesters,
                iconRes = R.drawable.ic_clock_primary,
                iconDescription = stringResource(R.string.set_date_and_time),
                showsFormat = true,
                onFieldChanged = { field, text -> viewModel.onFieldChanged(side, field, text) },
                onFieldFocused = onFieldFocused,
                onFieldLeft = { viewModel.onFieldLeft(side, it) },
                onFormatClick = { viewModel.onFormatClick(side) },
                onIconClick = {
                    focusManager.clearFocus()
                    onTimeIconClick()
                },
                error = timeError,
                enabled = entry.usesTime,
                hint = stringResource(R.string.date_time_hint_time_needs_full_date)
                    .takeUnless { entry.hasFullDate || entry.unknown || entry.hasQualifier }
            )
        }
    }

    // Hidden where the save can't store a zone (V1 only, e.g. Shared With Me).
    if (viewModel.savesZone) {
        TimeZoneRow(
            zone = entry.zone,
            offsetLabel = viewModel.zoneOffsetLabel(side),
            enabled = viewModel.canPickZone(side),
            onClick = {
                leaveFields()
                viewModel.onZoneRowClick(side)
            }
        )
    }

    if (showsOptions) {
        Column {
            listOf(
                DateQualifier.APPROXIMATE to R.string.date_time_approximate,
                DateQualifier.UNCERTAIN to R.string.date_time_uncertain,
                DateQualifier.UNKNOWN to R.string.date_time_unknown,
            ).forEach { (qualifier, titleRes) ->
                OptionSwitchRow(
                    title = stringResource(titleRes),
                    checked = when (qualifier) {
                        DateQualifier.APPROXIMATE -> entry.approximate
                        DateQualifier.UNCERTAIN -> entry.uncertain
                        DateQualifier.UNKNOWN -> entry.unknown
                    },
                    enabled = qualifier == DateQualifier.UNKNOWN || !entry.unknown
                ) {
                    leaveFields()
                    viewModel.onQualifierToggle(side, qualifier, it)
                }
            }
        }
    }
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
