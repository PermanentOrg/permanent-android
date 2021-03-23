package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class FileInfoViewModel(application: Application
) : ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener  {

    private val appContext = application.applicationContext
    private lateinit var fileData: FileData
    private val name = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private val address = MutableLiveData<String>()
    private val tags = MutableLiveData("")
    private val onShowDatePickerRequest = MutableLiveData<Void>()
    private val onShowLocationSearchRequest = MutableLiveData<Void>()
    private val showMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
        name.value = fileData.displayName
        description.value = fileData.description
        date.value = fileData.displayDate
        val streetName = if (fileData.streetName == null) "" else fileData.streetName + ", "
        val addressValue = (fileData.streetNumber ?: "") + " " + streetName + fileData.locality +
                ", " + fileData.adminOneName + ", " + fileData.countryCode
        if (!addressValue.contains("null")) address.value = addressValue
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

    fun onLocationClick() {
        onShowLocationSearchRequest.value = onShowLocationSearchRequest.value
    }

    fun onSaveClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val name = name.value?.trim()
        val description = description.value?.trim()
        val date = date.value

        if (name.isNullOrEmpty()) {
            showMessage.value = appContext.getString(R.string.invalid_name_error)
            return
        }

        fileData.displayName = name
        fileData.description = description
        fileData.displayDate = date

        isBusy.value = true
        fileRepository.updateRecord(fileData, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
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

    fun getAddress(): LiveData<String> {
        return address
    }

    fun getTags(): LiveData<String> {
        return tags
    }

    fun getShowDatePicker(): LiveData<Void> {
        return onShowDatePickerRequest
    }

    fun getShowLocationSearch(): LiveData<Void> {
        return onShowLocationSearchRequest
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getIsBusy(): LiveData<Boolean> {
        return isBusy
    }
}