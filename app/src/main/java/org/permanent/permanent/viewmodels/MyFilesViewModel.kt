package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.*
import java.util.*

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    FileClickListener, FileOptionsClickListener {

    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val existsFiles = MutableLiveData(false)
    private val isRoot = MutableLiveData(true)
    private val currentSearchQuery = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<RecordVO> = Stack()
    private var viewAdapter: FilesAdapter = FilesAdapter(this, this)
    private lateinit var recyclerView: RecyclerView
    private lateinit var fragmentManager: FragmentManager

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

    fun set(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    init {
        populateMyFiles()
    }

    private fun populateMyFiles() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        fileRepository.getMyFilesRecord(object : IFileRepository.IOnMyFilesArchiveNrListener {
            override fun onSuccess(myFilesRecord: RecordVO) {
                isBusy.value = false
                getChildRecordsOf(myFilesRecord)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
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

    fun getCurrentSearchQuery(): MutableLiveData<String>? {
        return currentSearchQuery
    }

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString().trim { it <= ' ' }
        viewAdapter.filter.filter(query)
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    override fun onFileClick(file: RecordVO) {
        if (file.typeEnum == RecordVO.Type.Folder)
            getChildRecordsOf(file)
    }

    override fun onFileOptionsClick(file: RecordVO) {
        val fragment = FileOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FILE_NAME, file.displayName)
        fragment.arguments = bundle
        showBottomSheetFragment(fragment)
    }

    fun onCurrentFolderClick() {
        val fragment = FolderOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FOLDER_NAME, Constants.MY_FILES_FOLDER)
        fragment.arguments = bundle
        showBottomSheetFragment(fragment)
    }

    fun onAddBtnClick() {
        val fragment = AddOptionsFragment()
        showBottomSheetFragment(fragment)
    }

    private fun showBottomSheetFragment(fragment: BottomSheetDialogFragment) {
        fragment.show(fragmentManager, fragment.tag)
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        val parentRecord = folderPathStack.pop()
        getChildRecordsOf(parentRecord)
    }
}