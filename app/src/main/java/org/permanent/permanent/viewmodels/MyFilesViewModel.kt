package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.myFiles.upload.UploadCancelClickListener
import org.permanent.permanent.ui.myFiles.upload.UploadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    UploadCancelClickListener, FileClickListener, FileOptionsClickListener, Upload.IOnFinishedListener {
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val existsUploads = MutableLiveData(false)
    private val existsFiles = MutableLiveData(false)
    private val isRoot = MutableLiveData(true)
    private val currentSearchQuery = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<RecordVO> = Stack()
    private var filesAdapter: FilesAdapter = FilesAdapter(this, this)
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var currentFolder: RecordVO
    private lateinit var uploadsRecyclerView: RecyclerView
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager

    fun set(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun initUploadsRecyclerView(rvUploads: RecyclerView, lifecycleOwner: LifecycleOwner) {
        uploadsRecyclerView = rvUploads
        uploadsAdapter = UploadsAdapter(appContext, lifecycleOwner, this)
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

    fun upload(owner: LifecycleOwner, uris: List<Uri>) {
        // persisting read uri permission
        for (uri in uris) {
            appContext.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val uploads = enqueueWork(owner, uris)
        uploadsAdapter.set(uploads)
        existsUploads.value = true
    }

    private fun enqueueWork(lifecycleOwner: LifecycleOwner, uris: List<Uri>): MutableList<Upload> {
        val uploadQueue = UploadQueue(appContext, Upload(appContext, uris[0], this))

        for (i in 1 until uris.size) {
            uploadQueue.add(Upload(appContext, uris[i], this))
        }
        return uploadQueue.enqueueOn(lifecycleOwner)
    }

    override fun onFinished(upload: Upload) {
        remove(upload)
        addFakeItemToFilesList(upload)
    }

    private fun remove(upload: Upload?) {
        uploadsAdapter.remove(upload)
        if (uploadsAdapter.isEmpty()) {
            existsUploads.value = false
            refreshCurrentFolder()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addFakeItemToFilesList(upload: Upload?) {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        val fakeFile = RecordVO()
        fakeFile.displayDT = currentDate
        fakeFile.displayName = upload?.displayName
        fakeFile.typeEnum = RecordVO.Type.Image
        filesAdapter.add(fakeFile)
    }

    override fun onCancelClick(upload: Upload) {
        TODO("Not yet implemented")
    }
}
