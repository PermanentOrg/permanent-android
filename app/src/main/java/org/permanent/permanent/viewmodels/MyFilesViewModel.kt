package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.*
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    UploadCancelClickListener, FileClickListener, FileOptionsClickListener {
    private val appContext = application.applicationContext
    private val workManager: WorkManager = WorkManager.getInstance()
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val existsUploads = MutableLiveData(false)
    private val existsFiles = MutableLiveData(false)
    private val isRoot = MutableLiveData(true)
    private val currentSearchQuery = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<RecordVO> = Stack()
    private var uploadsAdapter: UploadsAdapter = UploadsAdapter(appContext, this)
    private var filesAdapter: FilesAdapter = FilesAdapter(this, this)
    private lateinit var currentFolder: RecordVO
    private lateinit var uploadsRecyclerView: RecyclerView
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager

    fun set(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun initUploadsRecyclerView(rvUploads: RecyclerView) {
        uploadsRecyclerView = rvUploads
        uploadsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = uploadsAdapter
        }
    }

    fun initFilesRecyclerView(rvFiles: RecyclerView) {
        filesRecyclerView = rvFiles
        filesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = filesAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL)
            )
        }
    }

    fun initSwipeRefreshLayout(refreshLayout: SwipeRefreshLayout) {
        this.swipeRefreshLayout = refreshLayout
        swipeRefreshLayout.setOnRefreshListener { refreshCurrentFolder() }
        populateMyFiles()
    }

    fun refreshCurrentFolder() {
        getChildRecordsOf(currentFolder)
    }

    private fun populateMyFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getMyFilesRecord(object : IFileRepository.IOnMyFilesArchiveNrListener {
            override fun onSuccess(myFilesRecord: RecordVO) {
                swipeRefreshLayout.isRefreshing = false
                currentFolder = myFilesRecord
                folderPathStack.push(currentFolder)
                getChildRecordsOf(myFilesRecord)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                onErrorMessage.value = error
            }
        })
    }

    private fun getChildRecordsOf(parentRecord: RecordVO) {
        parentRecord.archiveNbr?.let {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.getChildRecordsOf(
                it,
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(records: List<RecordVO>?) {
                        swipeRefreshLayout.isRefreshing = false
                        val parentName = parentRecord.displayName
                        folderName.value = parentName
                        isRoot.value = parentName.equals(Constants.MY_FILES_FOLDER)

                        if (records != null) {
                            existsFiles.value = records.isNotEmpty()
                            filesAdapter.set(records)
                        }
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        onErrorMessage.value = error
                    }
                })
        }
    }

    fun getFolderName(): MutableLiveData<String> {
        return folderName
    }

    fun getExistsUploads(): MutableLiveData<Boolean> {
        return existsUploads
    }

    fun getExistsFiles(): MutableLiveData<Boolean> {
        return existsFiles
    }

    fun getIsRoot(): MutableLiveData<Boolean> {
        return isRoot
    }

    fun getCurrentSearchQuery(): MutableLiveData<String>? {
        return currentSearchQuery
    }

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString().trim { it <= ' ' }
        filesAdapter.filter.filter(query)
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    override fun onFileClick(file: RecordVO) {
        if (file.typeEnum == RecordVO.Type.Folder) {
            currentFolder = file
            folderPathStack.push(currentFolder)
            getChildRecordsOf(file)
        }
    }

    override fun onFileOptionsClick(file: RecordVO) {
        val fragment = FileOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FILE_NAME, file.displayName)
        fragment.arguments = bundle
        showBottomSheetFragment(fragment)
    }

    fun onFolderOptionsClick() {
        val fragment = FolderOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FOLDER_NAME, Constants.MY_FILES_FOLDER)
        fragment.arguments = bundle
        showBottomSheetFragment(fragment)
    }

    private fun showBottomSheetFragment(fragment: BottomSheetDialogFragment) {
        fragment.show(fragmentManager, fragment.tag)
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        val parentRecord = folderPathStack.pop()
        currentFolder = parentRecord
        folderPathStack.push(currentFolder)
        getChildRecordsOf(parentRecord)
    }

    fun upload(uris: List<Uri>): List<LiveData<WorkInfo>> {
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                appContext.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            existsUploads.value = true
            return setupUploadWorkers(uploadsAdapter.set(uris)).map {
                workManager.getWorkInfoByIdLiveData(it.uploadId) }
        }
        return emptyList()
    }

    private fun setupUploadWorkers(uploads: List<Upload>): List<Upload> {
        if (uploads.isNotEmpty()) {
            var workContinuation = workManager.beginWith(getUploadRequest(uploads[0]))

            for (i in 1 until uploads.size) {
                workContinuation = workContinuation.then(getUploadRequest(uploads[i]))
            }
            workContinuation.enqueue()
        }
        return uploads
    }

    private fun getUploadRequest(upload: Upload): OneTimeWorkRequest {
        val builder = Data.Builder().apply { putString(WORKER_INPUT_URI_KEY, upload.uri.toString()) }
        val workRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(WORKER_TAG_UPLOAD)
            .setInputData(builder.build())
            .build()
        upload.setId(workRequest.id)

        return workRequest
    }

    fun getUploadById(id: UUID): Upload? {
        for(upload in uploadsAdapter.getUploads()) {
            if (upload.uploadId == id) return upload
        }
        return null
    }

    fun removeUpload(upload: Upload?) {
        uploadsAdapter.remove(upload)
        if (uploadsAdapter.itemCount == 0) {
            existsUploads.value = false
            refreshCurrentFolder()
        }
    }

    fun refreshUploadsAdapter() {
        uploadsAdapter.notifyDataSetChanged()
    }

    override fun onCancelClick(upload: Upload) {
        TODO("Not yet implemented")
    }
}
