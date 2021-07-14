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
        uploaded.value = fileData.createdDate ?: "-"
        lastModified.value = fileData.updatedDate ?: "-"
        created.value = fileData.derivedDate ?: "-"
        fileCreated.value = fileData.derivedCreatedDate ?: "-"
        size.value = if (fileData.size != -1L) bytesToHumanReadable(fileData.size) else "-"
        fileType.value = fileData.contentType?.substringBefore("/") ?: "-"
        originalFileName.value = fileData.originalFileName ?: "-"
        originalFileType.value = fileData.originalFileType ?: "-"
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

    private fun bytesToHumanReadable(bytes: Long): String {
        val unit = 1024.0
        if (bytes < unit)
            return "$bytes B"
        var result = bytes.toDouble()
        val unitsToUse = "KMGTPE"
        var i = 0
        val unitsCount = unitsToUse.length
        while (true) {
            result /= unit
            if (result < unit || i == unitsCount - 1)
                break
            ++i
        }
        return with(StringBuilder(9)) {
            append(String.format("%.2f ", result))
            append(unitsToUse[i])
            append('B')
        }.toString()
    }
}