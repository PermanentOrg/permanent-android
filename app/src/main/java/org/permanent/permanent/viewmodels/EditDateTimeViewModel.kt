package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditDateTimeViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var records: MutableList<Record> = mutableListOf()
    var shouldClose: MutableState<Boolean> = mutableStateOf(false)
    var isBusy: MutableState<Boolean> = mutableStateOf(false)
    val showMessage = mutableStateOf("")

    private val onDateChanged = MutableLiveData<String>()
    private val currentTime = Calendar.getInstance()
    var initialDateMilis: Long = currentTime.timeInMillis
    var initialHour: Int = currentTime.get(Calendar.HOUR_OF_DAY)
    var initialMinute = currentTime.get(Calendar.MINUTE)

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        records.firstOrNull()?.displayDate?.let {
            val dateFormat = "yyyy-MM-dd HH:mm:ss"
            extractDateHourMinute(it, dateFormat)
        }
    }

    fun updateDate(dateString: String) {
        this.records.forEach {
            it.displayDate = dateString
        }
        applyChanges(dateString)
    }

    private fun applyChanges(dateString: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        fileRepository.updateMultipleRecords(records = records,
            isFolderRecordType = false,
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

    private fun extractDateHourMinute(dateString: String, dateFormat: String) {
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        val date = simpleDateFormat.parse(dateString)

        val hourFormatter = SimpleDateFormat("HH", Locale.getDefault())
        val minuteFormatter = SimpleDateFormat("mm", Locale.getDefault())

         date?.time?.let {
             initialDateMilis = it
        }
        hourFormatter.format(date).toIntOrNull()?.let {
            initialHour = it
        }
        minuteFormatter.format(date).toIntOrNull()?.let {
            initialMinute = it
        }
    }
}
