package org.permanent.permanent.viewmodels

import android.app.Application
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class EditDateTimeViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var records: MutableList<Record> = mutableListOf()

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
    }

    fun updateDate(dateString: String) {
        this.records.forEach {
            it.displayDate = dateString
        }
        applyChanges()
    }

    private fun applyChanges() {
//        toggleLoading()
        fileRepository.updateMultipleRecords(records = records,
            isFolderRecordType = false,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
//                    toggleLoading()
//                    triggerCloseScreen()
                }

                override fun onFailed(error: String?) {
//                    toggleLoading()
//                    error?.let {
//                        updateError(it)
//                    }
                }
            })
    }
}