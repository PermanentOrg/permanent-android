package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.models.EditedDate
import org.permanent.permanent.models.applyEditedDate
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.IFileRepository.RecordField
import org.permanent.permanent.ui.toInfoRowDate

class FileInfoViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private lateinit var fileData: FileData
    private val name = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private var pickedDate: EditedDate? = null
    private val address = MutableLiveData<String>()
    private val onShowDatePickerRequest = SingleLiveEvent<Void?>()
    val onShowLocationSearchRequest = SingleLiveEvent<Void?>()
    val onShowTagsEdit = SingleLiveEvent<Void?>()
    private val onFileInfoUpdated = SingleLiveEvent<String>()
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private val existsTags = MutableLiveData(false)
    private val hasCoordinates = MutableLiveData(false)
    private val isEditable = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
        isEditable.value = !(!CurrentArchivePermissionsManager.instance.isEditAvailable()
                || fileData.accessRole == AccessRole.VIEWER
                || fileData.accessRole == AccessRole.CONTRIBUTOR)
        name.value = fileData.displayName
        description.value = fileData.description
        refreshDate()
        address.value = fileData.completeAddress
        existsTags.value = !fileData.tags.isNullOrEmpty()
        hasCoordinates.value = fileData.hasCoordinates
    }

    fun onNameTextChanged(text: Editable) {
        name.value = text.toString()
    }

    fun onDescriptionTextChanged(text: Editable) {
        description.value = text.toString()
    }

    fun onDatePicked(value: EditedDate) {
        pickedDate = value
        saveChanges()
    }

    private fun refreshDate() {
        date.value = fileData.shownDate?.toInfoRowDate()
    }

    fun savedDate(): DisplayDated = fileData

    fun onDateClick() {
        if (isEditable.value == true)
            onShowDatePickerRequest.value = onShowDatePickerRequest.value
    }

    fun onLocationClick() {
        if (isEditable.value == true)
            onShowLocationSearchRequest.value = onShowLocationSearchRequest.value
    }

    fun onEditTagsClick() {
        if (isEditable.value == true)
            onShowTagsEdit.call()
    }

    fun saveChanges() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val nameValue = name.value?.trim()
        val description = description.value?.trim()

        if (nameValue.isNullOrEmpty()) {
            showError.value = appContext.getString(R.string.invalid_name_error)
            name.value = fileData.displayName
            return
        }

        val changedFields = buildSet {
            if (fileData.displayName != nameValue) add(RecordField.NAME)
            if (fileData.description != description) add(RecordField.DESCRIPTION)
            if (pickedDate != null) add(RecordField.DATE)
        }
        if (changedFields.isNotEmpty()) {
            fileData.displayName = nameValue
            fileData.description = description
            val retryOnV1 = pickedDate?.let { listOf(fileData).applyEditedDate(it) } ?: true
            if (pickedDate != null) refreshDate()
            pickedDate = null

            isBusy.value = true
            fileRepository.updateRecords(mutableListOf(fileData), changedFields, retryOnV1 = retryOnV1, listener = object : IResponseListener {
                override fun onSuccess(message: String?) {
                    message?.let { showMessage.value = it }
                    onFileInfoUpdated.value = nameValue
                    onSaveFinished()
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                    onSaveFinished()
                }
            })
        }
    }

    // Edits made while the previous save was running are saved now
    private fun onSaveFinished() {
        isBusy.value = false
        saveChanges()
    }

    fun getName(): LiveData<String> = name

    fun getDescription(): LiveData<String> = description

    fun getDate(): LiveData<String> = date

    fun getAddress(): LiveData<String> = address

    fun getShowDatePicker(): LiveData<Void?> = onShowDatePickerRequest

    fun getShowLocationSearch(): LiveData<Void?> = onShowLocationSearchRequest

    fun getOnFileInfoUpdated(): LiveData<String> = onFileInfoUpdated

    fun getShowTagsEdit(): LiveData<Void?> = onShowTagsEdit

    fun getShowError(): LiveData<String> = showError

    fun getShowMessage(): LiveData<String> = showMessage

    fun getExistsTags(): LiveData<Boolean> = existsTags

    fun getHasCoordinates(): LiveData<Boolean> = hasCoordinates

    fun getIsEditable(): LiveData<Boolean> = isEditable

    fun getIsBusy(): LiveData<Boolean> = isBusy
}