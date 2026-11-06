package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.models.EditedDate
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.applyEditedDate
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeEntry
import org.permanent.permanent.ui.bulkEditMetadata.DateTimeField
import org.permanent.permanent.ui.toInfoRowDate
import java.time.LocalDate

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
    val entry = mutableStateOf(DateTimeEntry())
    private var isTouched = false
    // "Adjust" only when the sheet edits one item that has a date; several files say "Add".
    var isAdjusting = false
        private set

    private val onDateChanged = MutableLiveData<EditedDate>()
    val today: LocalDate = LocalDate.now()

    fun setRecords(records: ArrayList<Record>) {
        if (isSetUp) return
        isSetUp = true
        this.records.addAll(records)
        val entries = records.map { DateTimeEntry.from(it) }.filterNot { it.isEmpty }.toSet()
        entries.singleOrNull()?.let { entry.value = it }
        isAdjusting = records.size == 1 && records.first().shownDate != null
    }

    fun setPickOnly(dated: DisplayDated) {
        if (isSetUp) return
        isSetUp = true
        isPickOnly = true
        entry.value = DateTimeEntry.from(dated)
        isAdjusting = dated.shownDate != null
    }

    val preview: String?
        get() = entry.value.shownDate?.toInfoRowDate()

    fun onFieldChanged(field: DateTimeField, text: String) {
        isTouched = true
        val changed = entry.value.with(field, text)
        entry.value = if (changed[field].length == field.width) changed.clamp(field, today) else changed
    }

    fun onFieldLeft(field: DateTimeField) {
        entry.value = entry.value.clamp(field, today)
    }

    fun onFormatClick() {
        isTouched = true
        entry.value = entry.value.copy(format = entry.value.format.next())
    }

    fun onDatePicked(date: LocalDate) {
        isTouched = true
        entry.value = entry.value.withDate(date)
    }

    fun onTimePicked(hour: Int, minute: Int, second: Int) {
        isTouched = true
        entry.value = entry.value.withTime(hour, minute, second)
    }

    fun onClearClick() {
        isTouched = true
        entry.value = DateTimeEntry(format = entry.value.format)
    }

    fun onSaveClick() {
        if (!isTouched) {
            shouldClose.value = true
            return
        }
        if (entry.value.toEditedDate() == null) return
        if (recordsCount > 1) showConfirmation.value = true else save()
    }

    fun onConfirmationDismissed() {
        showConfirmation.value = false
    }

    fun save() {
        showConfirmation.value = false
        val date = entry.value.toEditedDate() ?: return
        if (isPickOnly) {
            onDateChanged.value = date
            shouldClose.value = true
            return
        }
        if (isBusy.value) return
        val retryOnV1 = records.applyEditedDate(date)
        isBusy.value = true
        fileRepository.updateRecordDates(records, retryOnV1, object : IResponseListener {
            override fun onSuccess(message: String?) {
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
