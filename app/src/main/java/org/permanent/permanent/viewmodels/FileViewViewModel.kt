package org.permanent.permanent.viewmodels

import android.app.Application
import android.net.Uri
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
    private val fileUri = MutableLiveData<Uri>()
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
                fileUri.value = Uri.fromFile(file)
                controller.value = mediaController
            } else {
                filePath.value = Uri.fromFile(file).toString()
            }
        } else {
            filePath.value = fileData.downloadURL
            fileUri.value = Uri.parse(fileData.downloadURL)
        }
    }

    fun getShowingVideo(): MutableLiveData<Boolean> {
        return showingVideo
    }

    fun getFilePath(): MutableLiveData<String> {
        return filePath
    }

    fun getFileUri(): MutableLiveData<Uri> {
        return fileUri
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