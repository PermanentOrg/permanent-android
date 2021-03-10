package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileInfoViewModel(application: Application
) : ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener  {

    private val name = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private val tags = MutableLiveData("")
    private val onShowDatePickerRequest = MutableLiveData<Void>()
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
        name.value = fileData.displayName
        description.value = fileData.description
        date.value = fileData.displayDate
        val tags = fileData.tags
        if(!tags.isNullOrEmpty()) {
            for (tag in tags) {
                this.tags.value = this.tags.value +
                        if(this.tags.value?.isNotEmpty() == true) ", " + tag.name else tag.name
            }
        }
    }

    fun onNameTextChanged(text: Editable) {
        name.value = text.toString()
    }

    fun onDescriptionTextChanged(text: Editable) {
        description.value = text.toString()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        date.value = "$year-${month + 1}-$day"
    }

    fun onDateClick() {
        onShowDatePickerRequest.value = onShowDatePickerRequest.value
    }

    fun getName(): LiveData<String> {
        return name
    }

    fun getDescription(): LiveData<String> {
        return description
    }

    fun getDate(): LiveData<String> {
        return date
    }

    fun getTags(): LiveData<String> {
        return tags
    }

    fun getShowDatePicker(): LiveData<Void> {
        return onShowDatePickerRequest
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}