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
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Folder
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.FileClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsClickListener
import org.permanent.permanent.ui.myFiles.FolderOptionsFragment
import org.permanent.permanent.ui.myFiles.SortOptionsFragment
import org.permanent.permanent.ui.myFiles.download.DownloadCancelListener
import org.permanent.permanent.ui.myFiles.upload.UploadCancelListener
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    FileClickListener, FileOptionsClickListener, UploadCancelListener, Upload.IOnFinishedListener,
    DownloadCancelListener, Download.IOnFinishedListener {
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isRoot = MutableLiveData(true)
    private val isSortedAsc = MutableLiveData(true)

    private val currentSearchQuery = MutableLiveData<String>()
    private val existsFiles = MutableLiveData(false)
    private lateinit var existsDownloads: MutableLiveData<Boolean>
    private val onErrorMessage = MutableLiveData<String>()
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onFilesRetrieved = SingleLiveEvent<List<RecordVO>>()
    private val onFilesFilterQuery = MutableLiveData<Editable>()
    private val onNewTemporaryFile = SingleLiveEvent<RecordVO>()
    private val onShowAddOptionsFragment = SingleLiveEvent<FolderIdentifier>()
    private val onShowFileOptionsFragment = SingleLiveEvent<RecordVO>()

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
        uploadsAdapter = UploadsAdapter(lifecycleOwner, this)
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

    fun getFolderName(): MutableLiveData<String> {
        return folderName
    }

    fun setExistsDownloads(existsDownloads: MutableLiveData<Boolean>) {
        this.existsDownloads = existsDownloads
    }

    fun getExistsDownloads(): MutableLiveData<Boolean> {
        return existsDownloads
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

    fun getIsSortedAsc(): MutableLiveData<Boolean> {
        return isSortedAsc
    }

    fun getCurrentSearchQuery(): MutableLiveData<String>? {
        return currentSearchQuery
    }

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString().trim { it <= ' ' }
        onFilesFilterQuery.value = query
    }

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> {
        return onDownloadsRetrieved
    }

    fun getOnDownloadFinished(): MutableLiveData<Download> {
        return onDownloadFinished
    }

    fun getOnFilesRetrieved(): MutableLiveData<List<RecordVO>> {
        return onFilesRetrieved
    }

    fun getOnFilesFilterQuery(): MutableLiveData<Editable> {
        return onFilesFilterQuery
    }

    fun getOnNewTemporaryFile(): MutableLiveData<RecordVO> {
        return onNewTemporaryFile
    }

    fun getOnShowAddOptionsFragment(): MutableLiveData<FolderIdentifier> {
        return onShowAddOptionsFragment
    }

    fun onAddFabClick() {
        onShowAddOptionsFragment.value = currentFolder.getFolderIdentifier()
    }

    fun getOnShowFileOptionsFragment(): MutableLiveData<RecordVO> {
        return onShowFileOptionsFragment
    }

    override fun onFileOptionsClick(file: RecordVO) {
        onShowFileOptionsFragment.value = file
    }

    fun refreshCurrentFolder() {
        loadFilesOf(currentFolder)
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

    private fun populateMyFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getMyFilesRecord(object : IFileRepository.IOnMyFilesArchiveNrListener {
            override fun onSuccess(myFilesRecord: RecordVO) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(myFilesRecord)
                loadAllChildrenOf(myFilesRecord)
                loadEnqueuedDownloads(currentFolder, lifecycleOwner)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                onErrorMessage.value = error
            }
        })
    }

    private fun loadEnqueuedDownloads(folder: Folder, lifecycleOwner: LifecycleOwner) {
        folder.newDownloadQueue(lifecycleOwner, this)
            ?.getEnqueuedDownloadsLiveData()?.let { enqueuedDownloadsLiveData ->
                enqueuedDownloadsLiveData.observe(lifecycleOwner, { enqueuedDownloads ->
                    onDownloadsRetrieved.value = enqueuedDownloads
                })
            }
    }

    fun onFolderOptionsClick() {
        val fragment = FolderOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FOLDER_NAME, Constants.MY_FILES_FOLDER)
        fragment.arguments = bundle
        showBottomSheetFragment(fragment)
    }

    fun onSortOptionsClick() {
        val fragment = SortOptionsFragment()
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
        upload.removeWorkInfoObserver()
        uploadsAdapter.remove(upload)
        if (uploadsAdapter.itemCount == 0) {
            refreshCurrentFolder()
        }
        addFakeItemToFilesList(upload)
    }

    @SuppressLint("SimpleDateFormat")
    private fun addFakeItemToFilesList(upload: Upload?) {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        val fakeFile = RecordVO()
        fakeFile.displayDT = currentDate
        fakeFile.displayName = upload?.getDisplayName()
        fakeFile.typeEnum = RecordVO.Type.File
        onNewTemporaryFile.value = fakeFile
    }

    override fun onCancelClick(upload: Upload) {
        TODO("Not yet implemented")
    }

    fun download(file: RecordVO) {
        val uploadQueue = currentFolder.getDownloadQueue()
        uploadQueue?.enqueueNewDownloadFor(file)
    }

    override fun onFinished(download: Download) {
        download.removeWorkInfoObserver()
        onDownloadFinished.value = download
    }

    override fun onCancelClick(download: Download) {
        TODO("Not yet implemented")
    }
}
