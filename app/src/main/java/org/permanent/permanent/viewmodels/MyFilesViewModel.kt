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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Folder
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.FileClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsFragment
import org.permanent.permanent.ui.myFiles.FolderOptionsFragment
import org.permanent.permanent.ui.myFiles.upload.UploadCancelClickListener
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    UploadCancelClickListener, FileClickListener, FileOptionsClickListener, Upload.IOnFinishedListener {
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val existsFiles = MutableLiveData(false)
    private val isRoot = MutableLiveData(true)
    private val currentSearchQuery = MutableLiveData<String>()
    private val onErrorMessage = MutableLiveData<String>()
    private val onFilesRetrieved = SingleLiveEvent<List<RecordVO>>()
    private val onFilesFilterQuery = MutableLiveData<Editable>()
    private val onNewFile = MutableLiveData<RecordVO>()
    private val onShowAddOptionsFragment = SingleLiveEvent<FolderIdentifier>()

    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<RecordVO> = Stack()
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var currentFolder: Folder
    private lateinit var uploadsRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager
    private lateinit var lifecycleOwner: LifecycleOwner

    fun set(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun initUploadsRecyclerView(rvUploads: RecyclerView, lifecycleOwner: LifecycleOwner) {
        uploadsRecyclerView = rvUploads
        this.lifecycleOwner = lifecycleOwner
        uploadsAdapter = UploadsAdapter(appContext, lifecycleOwner, this)
        uploadsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = uploadsAdapter
        }
    }

    fun initSwipeRefreshLayout(refreshLayout: SwipeRefreshLayout) {
        this.swipeRefreshLayout = refreshLayout
        swipeRefreshLayout.setOnRefreshListener { refreshCurrentFolder() }
        populateMyFiles()
    }

    fun refreshCurrentFolder() {
        loadFilesOf(currentFolder)
    }

    private fun populateMyFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getMyFilesRecord(object : IFileRepository.IOnMyFilesArchiveNrListener {
            override fun onSuccess(myFilesRecord: RecordVO) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(myFilesRecord)
                loadAllChildrenOf(myFilesRecord)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                onErrorMessage.value = error
            }
        })
    }

    private fun loadFilesOf(folder: Folder) {
        folder.getArchiveNr()?.let {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.getChildRecordsOf(
                it,
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(records: List<RecordVO>?) {
                        swipeRefreshLayout.isRefreshing = false
                        val parentName = folder.getDisplayName()
                        folderName.value = parentName
                        isRoot.value = parentName.equals(Constants.MY_FILES_FOLDER)

                        if (records != null) {
                            existsFiles.value = records.isNotEmpty()
                            onFilesRetrieved.value = records
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
        return uploadsAdapter.getExistsUploads()
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
        onFilesFilterQuery.value = query
    }

    fun getOnFilesRetrieved(): MutableLiveData<List<RecordVO>> {
        return onFilesRetrieved
    }

    fun getOnFilesFilterQuery(): MutableLiveData<Editable> {
        return onFilesFilterQuery
    }

    fun getOnNewFile(): MutableLiveData<RecordVO> {
        return onNewFile
    }

    fun getOnShowAddOptionsFragment(): MutableLiveData<FolderIdentifier> {
        return onShowAddOptionsFragment
    }

    fun onAddFabClick() {
        onShowAddOptionsFragment.value = currentFolder.getFolderIdentifier()
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

    override fun onFileClick(file: RecordVO) {
        if (file.typeEnum == RecordVO.Type.Folder) {
            currentFolder.getUploadQueue()?.removeListeners()
            folderPathStack.push(file)
            loadAllChildrenOf(file)
        }
    }

    fun onBackBtnClick() {
        currentFolder.getUploadQueue()?.removeListeners()
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        val previousFolder = folderPathStack.pop()
        folderPathStack.push(previousFolder)
        loadAllChildrenOf(previousFolder)
    }

    private fun loadAllChildrenOf(folderInfo: RecordVO) {
        currentFolder = Folder(appContext, folderInfo)
        loadEnqueuedUploads(currentFolder, lifecycleOwner)
        loadFilesOf(currentFolder)
    }

    private fun loadEnqueuedUploads(folder: Folder, lifecycleOwner: LifecycleOwner) {
        folder.newUploadQueue(lifecycleOwner, this)
            ?.getEnqueuedUploadsLiveData()?.let { enqueuedUploadsLiveData ->
                enqueuedUploadsLiveData.observe(lifecycleOwner, { enqueuedUploads ->
                    uploadsAdapter.set(enqueuedUploads)
                })
            }
    }

    fun enqueueFilesForUpload(uris: List<Uri>) {
        val uploadQueue = currentFolder.getUploadQueue()
        for (uri in uris) {
            appContext.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uploadQueue?.addNewUploadFor(uri)
        }
        uploadQueue?.enqueuePendingUploads()
    }

    override fun onFinished(upload: Upload) {
        remove(upload)
        addFakeItemToFilesList(upload)
    }

    private fun remove(upload: Upload?) {
        uploadsAdapter.remove(upload)
        if (uploadsAdapter.itemCount == 0) {
            refreshCurrentFolder()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addFakeItemToFilesList(upload: Upload?) {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        val fakeFile = RecordVO()
        fakeFile.displayDT = currentDate
        fakeFile.displayName = upload?.getDisplayName()
        fakeFile.typeEnum = RecordVO.Type.Image
        onNewFile.value = fakeFile
    }

    override fun onCancelClick(upload: Upload) {
        TODO("Not yet implemented")
    }
}
