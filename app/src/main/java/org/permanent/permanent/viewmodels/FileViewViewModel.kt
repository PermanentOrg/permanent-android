package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileViewViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private lateinit var record: Record
    private var fileData = MutableLiveData<FileData>()
    private val filePath = MutableLiveData<String>()
    private val isVideo = MutableLiveData<Boolean>()
    private val isPDF = MutableLiveData<Boolean>()
    private val isError = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecord(record: Record) {
        this.record = record
        requestFileData()
    }

    private fun requestFileData() {
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId

        if (folderLinkId != null && recordId != null) {
            isBusy.value = true
            fileRepository.getRecord(folderLinkId, recordId).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    isError.value = false
                    fileData.value = response.body()?.getFileData()
                    fileData.value?.let { data ->
                        isPDF.value = data.contentType?.contains(FileType.PDF.toString())
                        isVideo.value = data.contentType?.contains(FileType.VIDEO.toString())

                        val externalFile = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            data.fileName
                        )

                        val cacheFile = File(
                            getApplication<Application>().cacheDir,
                            data.fileName
                        )

                        try {
                            if (externalFile.exists()) {
                                clearCache(getApplication())
                                externalFile.copyTo(cacheFile, overwrite = true)
                                filePath.value = "file://${cacheFile.absolutePath}"
                            } else if (data.contentType?.contains(FileType.IMAGE.toString()) == true) {
                                filePath.value = data.thumbURL2000
                            } else {
                                filePath.value = data.fileURL
                            }
                        } catch (e: Exception) {
                            Log.e("FileViewViewModel", "File copy failed", e)
                            filePath.value = data.fileURL // fallback
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    isError.value = true
                }
            })
        } else {
            Log.e("FileViewViewModel", "folderLinkId or recordId is null")
        }
    }

    fun clearCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { it.deleteRecursively() }
            }
        } catch (e: Exception) {
            Log.e("FileViewViewModel", "Failed to clear cache", e)
        }
    }

    fun onRetryBtnClick() {
        requestFileData()
    }

    fun getFileData(): MutableLiveData<FileData> = fileData

    fun getFilePath(): MutableLiveData<String> = filePath

    fun getIsVideo(): MutableLiveData<Boolean> = isVideo

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsPDF(): MutableLiveData<Boolean> = isPDF

    fun getIsError(): MutableLiveData<Boolean> = isError
}