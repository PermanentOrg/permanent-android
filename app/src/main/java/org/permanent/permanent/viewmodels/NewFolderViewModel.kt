package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class NewFolderViewModel (application: Application) : ObservableAndroidViewModel(application) {
    private lateinit var alertDialog: AlertDialog
    private val currentFolderName = MutableLiveData<String>()
    private val nameError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onFolderCreated = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    val errorStringId = MutableLiveData<Int>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setDialog(alert: AlertDialog) {
       alertDialog = alert
    }

    fun getCurrentFolderName(): MutableLiveData<String>? {
        return currentFolderName
    }

    fun getNameError(): LiveData<Int> {
        return nameError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnFolderCreated(): MutableLiveData<Void> {
        return onFolderCreated
    }

    fun getErrorStringId(): LiveData<Int> {
        return errorStringId
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

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

    fun onCreateBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val folderName = getValidatedFolderName()

        folderName?.let {
            isBusy.value = true
            fileRepository.createFolder(
                it,
                object : IFileRepository.IOnFolderCreatedListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        alertDialog.dismiss()
                        onFolderCreated.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        alertDialog.dismiss()
                        when (error) {
                            Constants.ERROR_SERVER_ERROR -> errorStringId.value =
                                R.string.server_error
                            else -> errorMessage.value = error
                        }
                    }
                })
        }
    }

    fun onCancelBtnClick() {
        alertDialog.dismiss()
    }
}
