package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.permanent.permanent.FeatureFlags
import org.permanent.permanent.R
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.models.EditedDate
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.applyEditedDate
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.bulkEditMetadata.DateQualifier
import org.permanent.permanent.ui.bulkEditMetadata.DateRangeEntry
import org.permanent.permanent.ui.bulkEditMetadata.DateRangeError
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeEntry
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeField
import org.permanent.permanent.ui.bulkEditMetadata.RangeSide
import org.permanent.permanent.ui.bulkEditMetadata.TimeZoneCatalog
import org.permanent.permanent.ui.bulkEditMetadata.TimeZoneOption
import org.permanent.permanent.ui.bulkEditMetadata.TimeZoneRegion
import java.time.LocalDate
import java.time.LocalDateTime

class EditDateTimeViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var records: MutableList<Record> = mutableListOf()
    // Pick-only: the caller saves the picked value for its one file.
    private var isPickOnly = false
    private var isSetUp = false
    val recordsCount: Int
        get() = if (isPickOnly) 1 else records.size
    var shouldClose: MutableState<Boolean> = mutableStateOf(false)
    var isBusy: MutableState<Boolean> = mutableStateOf(false)
    val showMessage = mutableStateOf("")
    val showConfirmation = mutableStateOf(false)
    val range = mutableStateOf(DateRangeEntry())
    // The full view: the range switch and each side's toggles.
    val showsOptions = mutableStateOf(false)
    private var isTouched = false
    // "Adjust" only when the sheet edits one item that has a date; several files say "Add".
    var isAdjusting = false
        private set

    // Only a V2 save stores a zone, a qualifier or a range; otherwise their rows are hidden.
    var savesZone = false
        private set
    // The zone the sheet opened with; unsetting it clears the record's zone.
    private var openingZone: String? = null
    val showZonePicker = mutableStateOf(false)
    val zoneQuery = mutableStateOf("")
    val zoneRegions = mutableStateOf<List<TimeZoneRegion>>(emptyList())
    private var zoneSearch: Job? = null

    private val onDateChanged = MutableLiveData<EditedDate>()
    val today: LocalDate = LocalDate.now()

    // Several files open without a zone; only one file shows its own.
    fun setRecords(records: ArrayList<Record>) {
        if (isSetUp) return
        this.records.addAll(records)
        val savesZone = fileRepository.canPatchOnV2(records)
        val entries = records.map { DateRangeEntry.from(it, full = savesZone) }.filterNot { it.isEmpty }.toSet()
        val common = entries.singleOrNull() ?: DateRangeEntry()
        setUp(
            if (records.size == 1) common.withZone(records.first().timezone) else common,
            savesZone,
            isAdjusting = records.size == 1 && records.first().hasDate
        )
    }

    fun setPickOnly(dated: DisplayDated, savesZone: Boolean) {
        if (isSetUp) return
        isPickOnly = true
        setUp(DateRangeEntry.from(dated, full = savesZone).withZone(dated.timezone), savesZone, dated.hasDate)
    }

    private fun setUp(opening: DateRangeEntry, savesZone: Boolean, isAdjusting: Boolean) {
        isSetUp = true
        range.value = opening
        openingZone = opening.start.zoneId
        this.savesZone = savesZone
        this.isAdjusting = isAdjusting
        showsOptions.value = savesZone && opening.opensFull
    }

    fun entry(side: RangeSide): DateTimeEntry = range.value[side] ?: DateTimeEntry()

    val preview: String?
        get() = range.value.preview(getApplication<Application>().getString(R.string.date_time_unknown))

    val errorRes: Int?
        get() = when (range.value.error) {
            DateRangeError.END_BEFORE_START -> R.string.date_time_error_end_before_start
            DateRangeError.NOT_ACCEPTED -> R.string.date_time_error_not_accepted
            null -> null
        }

    fun canPickZone(side: RangeSide): Boolean = savesZone && entry(side).canPickZone

    val canClear: Boolean
        get() = FeatureFlags.allowsClearingDate && savesZone

    // An empty sheet clears the date, so it saves only while clearing is allowed.
    val canSave: Boolean
        get() = range.value.let { (canClear || !it.isEmpty) && it.toEditedDate(openingZone) != null }

    // The side whose clock the zone list's offsets are taken at.
    private var zoneSide = RangeSide.START
    private val offsetClock: LocalDateTime
        get() = clockAt(zoneSide)

    private fun clockAt(side: RangeSide): LocalDateTime = entry(side).wallClock() ?: LocalDateTime.now()

    private fun edit(change: (DateRangeEntry) -> DateRangeEntry) {
        isTouched = true
        range.value = change(range.value)
    }

    private fun change(side: RangeSide, change: (DateTimeEntry) -> DateTimeEntry) =
        edit { it.with(side, change(entry(side))) }

    fun onZoneRowClick(side: RangeSide) {
        if (!canPickZone(side)) return
        zoneSide = side
        zoneQuery.value = ""
        searchZones()
        showZonePicker.value = true
    }

    fun onZoneQueryChanged(query: String) {
        zoneQuery.value = query
        searchZones()
    }

    private fun searchZones() {
        val query = zoneQuery.value
        val clock = offsetClock
        zoneSearch?.cancel()
        zoneSearch = viewModelScope.launch {
            zoneRegions.value = withContext(Dispatchers.Default) { TimeZoneCatalog.regions(clock, query) }
        }
    }

    fun onZonePicked(id: String?) {
        edit { it.withZone(id) }
        showZonePicker.value = false
    }

    fun onZonePickerBack() {
        showZonePicker.value = false
    }

    // In the zone list.
    fun zoneOffsetLabel(option: TimeZoneOption): String = option.offsetLabel(offsetClock)

    // On a side's zone row.
    fun zoneOffsetLabel(side: RangeSide): String? = entry(side).zone?.offsetLabel(clockAt(side))

    fun onFieldChanged(side: RangeSide, field: DateTimeField, text: String) = change(side) {
        val changed = it.with(field, text)
        if (changed[field].length == field.width) changed.clamp(field, today) else changed
    }

    fun onFieldLeft(side: RangeSide, field: DateTimeField) {
        range.value = range.value.with(side, entry(side).clamp(field, today))
    }

    fun onFormatClick(side: RangeSide) = change(side) { it.copy(format = it.format.next()) }

    fun onDatePicked(side: RangeSide, date: LocalDate) = change(side) { it.withDate(date) }

    fun onTimePicked(side: RangeSide, hour: Int, minute: Int, second: Int) = change(side) { it.withTime(hour, minute, second) }

    fun onOptionsClick() {
        showsOptions.value = !showsOptions.value
    }

    fun onQualifierToggle(side: RangeSide, qualifier: DateQualifier, on: Boolean) =
        edit { it.withQualifier(side, qualifier, on) }

    fun onRangeToggle(on: Boolean) = edit { it.withRange(on) }

    fun onClearClick() = edit { it.cleared() }

    fun onSideClearClick(side: RangeSide) = edit { it.cleared(side) }

    fun onSaveClick() {
        if (!isTouched) {
            shouldClose.value = true
            return
        }
        if (!canSave) return
        if (recordsCount > 1) showConfirmation.value = true else save()
    }

    fun onConfirmationDismissed() {
        showConfirmation.value = false
    }

    fun save() {
        showConfirmation.value = false
        val date = range.value.toEditedDate(openingZone) ?: return
        if (isPickOnly) {
            onDateChanged.value = date
            shouldClose.value = true
            return
        }
        if (isBusy.value) return
        val retryOnV1 = records.applyEditedDate(date)
        isBusy.value = true
        fileRepository.updateRecordDates(records, date.zone, retryOnV1, object : IResponseListener {
            override fun onSuccess(message: String?) {
                records.forEach { it.applyZone(date.zone) }
                isBusy.value = false
                shouldClose.value = true
                onDateChanged.value = date
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showMessage.value = it }
            }
        })
    }

    fun getOnDateChanged() = onDateChanged
}
