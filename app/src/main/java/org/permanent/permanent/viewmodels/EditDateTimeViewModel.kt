package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.toLocalDateTimeOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class EditDateTimeViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var records: MutableList<Record> = mutableListOf()
    var shouldClose: MutableState<Boolean> = mutableStateOf(false)
    var isBusy: MutableState<Boolean> = mutableStateOf(false)
    val showMessage = mutableStateOf("")

    private val onDateChanged = MutableLiveData<String>()
    private val now = LocalDateTime.now()
    // The Material3 DatePicker works in UTC-midnight millis.
    var initialDateMilis: Long = now.toLocalDate().toUtcMillis()
    var initialHour: Int = now.hour
    var initialMinute = now.minute

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        records.firstOrNull()?.shownDate?.let { seedPicker(it) }
    }

    fun updateDate(dateString: String) {
        this.records.forEach { it.setEditedDate(dateString) }
        applyChanges(dateString)
    }

    private fun applyChanges(dateString: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        fileRepository.updateRecordDates(records,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    shouldClose.value = true
                    onDateChanged.value = dateString
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let {
                        showMessage.value = it
                    }
                }
            })
    }

    fun getOnDateChanged() = onDateChanged

    // Values that are not a full date (e.g. "1985", "1985-10~") keep the defaults.
    private fun seedPicker(shownDate: String) {
        val dateTime = shownDate.toLocalDateTimeOrNull() ?: return
        initialDateMilis = dateTime.toLocalDate().toUtcMillis()
        if (shownDate.length > 10) {
            initialHour = dateTime.hour
            initialMinute = dateTime.minute
        }
    }

    private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
