package org.permanent.permanent.viewmodels

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData
import java.io.File

class FileViewViewModel(application: Application)
    : ObservableAndroidViewModel(application), MediaPlayer.OnInfoListener {

    private val filePath = MutableLiveData<String>()
    private lateinit var videoUri: Uri
    val showingVideo = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileData.fileName)

        if (fileData.contentType?.contains("video") == false) {
            filePath.value = if (file.exists()) Uri.fromFile(file).toString() else fileData.fileURL
        } else {
            showingVideo.value = true
            videoUri = if (file.exists()) Uri.fromFile(file) else Uri.parse(fileData.fileURL)
        }
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                isBusy.value = false
                return true
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                isBusy.value = true
                return true
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                isBusy.value = false
                return true
            }
        }
        return false
    }

    fun getFilePath(): MutableLiveData<String> {
        return filePath
    }

    fun getVideoUri(): Uri {
        return videoUri
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}