package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Environment
import android.util.Log
import android.widget.MediaController
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData
import java.io.File

class FileViewViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val controller = MutableLiveData<MediaController>()
    private val filePath = MutableLiveData<String>()
    private val showingVideo = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()

    fun init(fileData: FileData, mediaController: MediaController) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileData.fileName
        )

        Log.e(FileViewViewModel::class.java.simpleName, "file exists: " + file.exists())
        if (file.exists()) {
            if (fileData.contentType?.contains("video") == true) {
                showingVideo.value = true
                filePath.value = file.path
                controller.value = mediaController
            } else {
                filePath.value = "file://" + file.path
            }
        } else {
            filePath.value = fileData.downloadURL
            Log.e(FileViewViewModel::class.java.simpleName, "filePath.value: " + filePath.value)
            Log.e(FileViewViewModel::class.java.simpleName, "fileData.downloadURL: " + fileData.downloadURL)
        }
    }

    fun getShowingVideo(): MutableLiveData<Boolean> {
        return showingVideo
    }

    fun getFilePath(): MutableLiveData<String> {
        return filePath
    }

    fun getController(): MutableLiveData<MediaController> {
        return controller
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}