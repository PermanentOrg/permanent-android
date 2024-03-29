package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.bytesToHumanReadableString
import java.text.SimpleDateFormat
import java.util.*

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

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun setFileData(fileData: FileData) {
        uploaded.value = formattedDate(fileData.createdDate) ?: "-"
        lastModified.value = formattedDate(fileData.updatedDate) ?: "-"
        created.value = formattedDate(fileData.derivedDate) ?: "-"
        fileCreated.value = formattedDate(fileData.derivedCreatedDate) ?: "-"
        size.value = if (fileData.size != -1L) bytesToHumanReadableString(fileData.size) else "-"
        fileType.value = fileData.contentType?.substringBefore("/") ?: "-"
        originalFileName.value = fileData.originalFileName ?: "-"
        originalFileType.value = fileData.originalFileType ?: "-"
        width.value = if (fileData.width != -1) fileData.width.toString() else "-"
        height.value = if (fileData.height != -1) fileData.height.toString() else "-"
    }

    fun formattedDate(date: String?): String? {
        if (date != null) {
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

            val parsedDate = dateFormat.parse(date)

            dateFormat.applyPattern("yyyy-MM-dd h:mm a zzz")
            dateFormat.setTimeZone(TimeZone.getDefault())

            return dateFormat.format(parsedDate)
        } else {
            return null
        }
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