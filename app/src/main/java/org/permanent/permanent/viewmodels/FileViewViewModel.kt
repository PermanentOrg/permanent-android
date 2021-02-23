package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData
import java.io.File

class FileViewViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val recordName = MutableLiveData<String>()
    private val filePath = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()

    fun setFileData(fileData: FileData) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileData.fileName)

        Log.e(FileViewViewModel::class.java.simpleName, "file exists: " + file.exists())
        if (file.exists()) {
            filePath.value = "file://" + file.path
        } else {
            filePath.value = fileData.downloadURL
            Log.e(FileViewViewModel::class.java.simpleName, "filePath.value: " + filePath.value)
            Log.e(FileViewViewModel::class.java.simpleName, "fileData.downloadURL: " + fileData.downloadURL)
        }
    }

    fun getFilePath(): MutableLiveData<String> {
        return filePath
    }

    fun getName(): MutableLiveData<String> {
        return recordName
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}