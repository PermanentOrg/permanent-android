package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord

class SharesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val onSharesByMeRetrieved = SingleLiveEvent<MutableList<DownloadableRecord>>()
    private val onSharesWithMeRetrieved = SingleLiveEvent<MutableList<DownloadableRecord>>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    init {
        getShares()
    }

    fun getShares() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.getShares(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val sharesByMe: MutableList<DownloadableRecord> = ArrayList()
                    val sharesWithMe: MutableList<DownloadableRecord> = ArrayList()
                    val userArchiveId = PreferencesHelper(appContext
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)).getArchiveId()

                    for (data in dataList) {
                        val archive = data.ArchiveVO
                        val items = archive?.ItemVOs

                        items?.let {
                            for (item in it) {
                                val shareItem: DownloadableRecord
                                if (userArchiveId == archive.archiveId) {
                                    shareItem = DownloadableRecord(item, archive, false)
                                    sharesByMe.add(shareItem)
                                } else {
                                    shareItem = DownloadableRecord(item, archive, true)
                                    sharesWithMe.add(shareItem)
                                }
                            }
                        }
                    }
                    if (sharesByMe.isNotEmpty()) onSharesByMeRetrieved.value = sharesByMe
                    if (sharesWithMe.isNotEmpty()) onSharesWithMeRetrieved.value = sharesWithMe
                }
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

    fun getOnSharesByMeRetrieved(): LiveData<MutableList<DownloadableRecord>> {
        return onSharesByMeRetrieved
    }

    fun getOnSharesWithMeRetrieved(): LiveData<MutableList<DownloadableRecord>> {
        return onSharesWithMeRetrieved
    }
}