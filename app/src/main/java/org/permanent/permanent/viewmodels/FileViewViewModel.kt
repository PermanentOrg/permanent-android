package org.permanent.permanent.viewmodels

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.models.FileType
import org.permanent.permanent.network.models.FileData
import java.io.File

class FileViewViewModel(application: Application
) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var file: File? = null
    private val filePath = MutableLiveData<String>()
    private val isVideo = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
        isVideo.value = fileData.contentType?.contains(FileType.VIDEO.toString())
        fileData.fileName?.let {
            file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), it)
            filePath.value = if (file?.exists() == true) Uri.fromFile(file).toString() else fileData.fileURL
        }
    }

    fun getFilePath(): MutableLiveData<String> {
        return filePath
    }

    fun getIsVideo(): MutableLiveData<Boolean> {
        return isVideo
    }

    fun getUriForSharing(): Uri? {
        return file?.let { FileProvider.getUriForFile(appContext, Constants.FILE_PROVIDER_NAME, it) }
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}