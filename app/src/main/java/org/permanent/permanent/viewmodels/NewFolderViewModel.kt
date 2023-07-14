package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class NewFolderViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentFolderName = MutableLiveData<String>()
    private val nameError = MutableLiveData<Int?>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onFolderCreated = SingleLiveEvent<Void?>()
    private val errorMessage = MutableLiveData<String>()
    val errorStringId = MutableLiveData<Int>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun onNameTextChanged(email: Editable) {
        currentFolderName.value = email.toString()
    }

    private fun getValidatedFolderName(): String? {
        val name = currentFolderName.value?.trim()

        if (name.isNullOrEmpty()) {
            nameError.value = R.string.invalid_folder_name_error
            return null
        }
        nameError.value = null
        return name
    }

    fun createNewFolder(parentFolderIdentifier: NavigationFolderIdentifier?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val folderName = getValidatedFolderName()

        if (parentFolderIdentifier != null && folderName != null) {
            isBusy.value = true
            fileRepository.createFolder(
                parentFolderIdentifier,
                folderName,
                object : IRecordListener {
                    override fun onSuccess(record: Record) {
                        isBusy.value = false
                        onFolderCreated.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        when (error) {
                            Constants.ERROR_SERVER_ERROR -> errorStringId.value =
                                R.string.server_error
                            else -> error?.let { errorMessage.value = it }
                        }
                    }
                })
        }
    }

    fun getCurrentFolderName(): MutableLiveData<String> = currentFolderName

    fun getNameError(): LiveData<Int?> = nameError

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnFolderCreated(): MutableLiveData<Void?> = onFolderCreated

    fun getErrorStringId(): LiveData<Int> = errorStringId

    fun getErrorMessage(): LiveData<String> = errorMessage
}
