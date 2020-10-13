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
import org.permanent.permanent.ui.myFiles.FileOptionsClickListener
import org.permanent.permanent.ui.myFiles.FileOptionsFragment
import org.permanent.permanent.ui.myFiles.FilesAdapter

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    FileOptionsClickListener {

    private val appContext = application.applicationContext
    private val existsFiles = MutableLiveData(false)
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FilesAdapter

    init {
        fileRepository.getRecordVOs(object : IFileRepository.IOnRecordsRetrievedListener {
            override fun onSuccess(records: List<RecordVO>?) {
                if (records != null && records.isNotEmpty()) {
                    existsFiles.value = true
                    setupRecyclerView(records)
                }
            }

            override fun onFailed(error: String?) {
                onErrorMessage.value = error
            }
        })
    }

    fun initRecyclerView(rvFiles: RecyclerView) {
        recyclerView = rvFiles
    }

    fun getExistsFiles(): MutableLiveData<Boolean> {
        return existsFiles
    }

    fun setupRecyclerView(records: List<RecordVO>) {
        viewAdapter = FilesAdapter(records, this)
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

    override fun onFileOptionsClick(file: RecordVO) {
        val bottomDrawerFragment = FileOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FILE_NAME, file.displayName)
        bottomDrawerFragment.arguments = bundle
        bottomDrawerFragment.show((appContext as AppCompatActivity).supportFragmentManager,
            bottomDrawerFragment.tag)
    }
}