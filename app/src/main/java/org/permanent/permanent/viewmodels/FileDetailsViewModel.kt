package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileDetailsViewModel(application: Application) : ObservableAndroidViewModel(application) {

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
        size.value = fileData.size?.let { it.toString() } ?: "-"
        fileType.value = fileData.contentType?.substringBefore("/")?.let { it } ?: "-"
        originalFileName.value = fileData.originalFileName?.let { it } ?: "-"
        originalFileType.value = fileData.originalFileType?.let { it } ?: "-"
        width.value = fileData.width?.let { it.toString() } ?: "-"
        height.value = fileData.height?.let { it.toString() } ?: "-"
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