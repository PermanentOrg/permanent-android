package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository

class CreateNewArchiveViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private var archiveType: ArchiveType? = null
    private val currentName = MutableLiveData<String>()
    private val nameError = MutableLiveData<Int?>()
    private val archiveTypeError = MutableLiveData<Int?>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onArchiveCreatedResult = SingleLiveEvent<Void>()
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun getCurrentName(): MutableLiveData<String> = currentName

    fun getNameError(): LiveData<Int?> = nameError

    fun getArchiveTypeError(): LiveData<Int?> = archiveTypeError

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString()
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnArchiveCreatedResult(): LiveData<Void> = onArchiveCreatedResult

    fun getShowMessage(): LiveData<String> = showMessage

    fun getShowError(): LiveData<String> = showError

    fun clearFields() {
        currentName.value = ""
        archiveType = null
    }

    fun setArchiveType(type: ArchiveType) {
        archiveType = type
        archiveTypeError.value = null
    }

    fun onCreateBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = getValidatedName()
        val archiveType = getValidatedArchiveType()

        if (name != null && archiveType != null) {
            isBusy.value = true
            archiveRepository.createNewArchive(name, archiveType, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    onArchiveCreatedResult.call()
                    message?.let { showMessage.value = it }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onArchiveCreatedResult.call()
                    error?.let { showError.value = it }
                }
            })
        }
    }

    private fun getValidatedName(): String? {
        val name = currentName.value
        if (name.isNullOrEmpty()) {
            nameError.value = R.string.invalid_name_error
            return null
        }
        nameError.value = null
        return name
    }

    private fun getValidatedArchiveType(): ArchiveType? {
        if (archiveType == null) {
            archiveTypeError.value = R.string.invalid_archive_type_error
            return null
        }
        archiveTypeError.value = null
        return archiveType
    }
}
