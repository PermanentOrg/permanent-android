package org.permanent.permanent.viewmodels

import android.app.Application
import android.net.Uri
import android.os.Environment
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
    private var file: File? = null
    private var fileData = MutableLiveData<FileData>()
    private val filePath = MutableLiveData<String>()
    private val isVideo = MutableLiveData<Boolean>()
    private val isPDF = MutableLiveData<Boolean>()
    private val isError = MutableLiveData<Boolean>(false)
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
                    fileData.value?.let {
                        isPDF.value = it.contentType?.contains(FileType.PDF.toString())
                        isVideo.value = it.contentType?.contains(FileType.VIDEO.toString())
                        file = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                            ), it.fileName
                        )
                        filePath.value = if (file?.exists() == true)
                            Uri.fromFile(file).toString() else it.fileURL
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    isError.value = true
                }
            })
        }
    }

    fun onRetryBtnClick(){
        requestFileData()
    }

    fun getFileData(): MutableLiveData<FileData> = fileData

    fun getFilePath(): MutableLiveData<String> = filePath

    fun getIsVideo(): MutableLiveData<Boolean> = isVideo

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsPDF(): MutableLiveData<Boolean> = isPDF

    fun getIsError(): MutableLiveData<Boolean> = isError
}