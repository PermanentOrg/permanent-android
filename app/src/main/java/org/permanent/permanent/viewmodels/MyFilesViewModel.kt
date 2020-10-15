package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.FileClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsFragment
import org.permanent.permanent.ui.myFiles.FilesAdapter
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    FileClickListener, FileOptionsClickListener {

    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val existsFiles = MutableLiveData(false)
    private val isRoot = MutableLiveData(true)
    private val isBusy = MutableLiveData<Boolean>()
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<RecordVO> = Stack()
    private var viewAdapter: FilesAdapter = FilesAdapter(this, this)
    private lateinit var recyclerView: RecyclerView

    fun initRecyclerView(rvFiles: RecyclerView) {
        recyclerView = rvFiles
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = viewAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL)
            )
        }
    }

    init {
        populateMyFiles()
    }

    private fun populateMyFiles() {
        fileRepository.getMyFilesRecord(object : IFileRepository.IOnMyFilesArchiveNrListener {
            override fun onSuccess(myFilesRecord: RecordVO) {
                getChildRecordsOf(myFilesRecord)
            }

            override fun onFailed(error: String?) {
                onErrorMessage.value = error
            }
        })
    }

    private fun getChildRecordsOf(parentRecord: RecordVO) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        parentRecord.archiveNbr?.let {
            isBusy.value = true
            folderPathStack.push(parentRecord)
            fileRepository.getChildRecordsOf(it, object : IFileRepository.IOnRecordsRetrievedListener {
                override fun onSuccess(records: List<RecordVO>?) {
                    isBusy.value = false
                    val parentName = parentRecord.displayName
                    folderName.value = parentName
                    isRoot.value = parentName.equals(Constants.MY_FILES_FOLDER)

                    if (records != null) {
                        existsFiles.value = records.isNotEmpty()
                        viewAdapter.set(records)
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onErrorMessage.value = error
                }
            })
        }
    }

    fun getFolderName(): MutableLiveData<String> {
        return folderName
    }

    fun getExistsFiles(): MutableLiveData<Boolean> {
        return existsFiles
    }

    fun getIsRoot(): MutableLiveData<Boolean> {
        return isRoot
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    override fun onFileClick(file: RecordVO) {
        if (file.typeEnum == RecordVO.Type.Folder)
            getChildRecordsOf(file)
    }

    override fun onFileOptionsClick(file: RecordVO) {
        val bottomDrawerFragment = FileOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FILE_NAME, file.displayName)
        bottomDrawerFragment.arguments = bundle
        bottomDrawerFragment.show((appContext as AppCompatActivity).supportFragmentManager,
            bottomDrawerFragment.tag)
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        val parentRecord = folderPathStack.pop()
        getChildRecordsOf(parentRecord)
    }
}