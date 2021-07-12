package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.format.Formatter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileDetailsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val uploaded = MutableLiveData("-")
    private val lastModified = MutableLiveData("-")
    private val created = MutableLiveData("-")
    private val fileCreated = MutableLiveData("-")
    private val size = MutableLiveData("-")
    private val fileType = MutableLiveData("-")
    private val originalFileName = MutableLiveData("-")
    private val originalFileType = MutableLiveData("-")
    private val width = MutableLiveData("-")
    private val height = MutableLiveData("-")

    fun setFileData(fileData: FileData) {
        uploaded.value = fileData.createdDate?.let { it } ?: "-"
        lastModified.value = fileData.updatedDate?.let { it } ?: "-"
        created.value = fileData.derivedDate?.let { it } ?: "-"
        fileCreated.value = fileData.derivedCreatedDate?.let { it } ?: "-"
        size.value =
            if (fileData.size != -1L) Formatter.formatFileSize(appContext, fileData.size) else "-"
        fileType.value = fileData.contentType?.substringBefore("/")?.let { it } ?: "-"
        originalFileName.value = fileData.originalFileName?.let { it } ?: "-"
        originalFileType.value = fileData.originalFileType?.let { it } ?: "-"
        width.value = if (fileData.width != -1) fileData.width.toString() else "-"
        height.value = if (fileData.height != -1) fileData.height.toString() else "-"
    }

    fun getUploaded(): LiveData<String> {
        return uploaded
    }

    fun getLastModified(): LiveData<String> {
        return lastModified
    }

    fun getCreated(): LiveData<String> {
        return created
    }

    fun getFileCreated(): LiveData<String> {
        return fileCreated
    }

    fun getSize(): LiveData<String> {
        return size
    }

    fun getFileType(): LiveData<String> {
        return fileType
    }

    fun getOriginalFileName(): LiveData<String> {
        return originalFileName
    }

    fun getOriginalFileType(): LiveData<String> {
        return originalFileType
    }

    fun getWidth(): LiveData<String> {
        return width
    }

    fun getHeight(): LiveData<String> {
        return height
    }
}