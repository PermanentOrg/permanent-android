package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class SharesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val onSharedArchivesRetrieved = MutableLiveData<List<Datum>>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(appContext)

    init {
        getShares()
    }

    private fun getShares() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.getShares(object : IFileRepository.IOnSharesListener {
            override fun onSuccess(shareArchives: List<Datum>?) {
                isBusy.value = false
                if (!shareArchives.isNullOrEmpty()) onSharedArchivesRetrieved.value = shareArchives
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getOnSharedArchivesRetrieved(): LiveData<List<Datum>> {
        return onSharedArchivesRetrieved
    }
}