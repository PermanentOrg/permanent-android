package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SharesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val onSharesByMeRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onSharesWithMeRetrieved = SingleLiveEvent<MutableList<Record>>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun requestShares() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.getShares(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val sharesByMe: MutableList<Record> = ArrayList()
                    val sharesWithMe: MutableList<Record> = ArrayList()
                    val currentArchiveId = PreferencesHelper(
                        appContext
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    ).getCurrentArchiveId()

                    for (data in dataList) {
                        val archive = data.ArchiveVO
                        val items = archive?.ItemVOs

                        items?.let {
                            for (item in it) {
                                val shareItem: Record
                                if (currentArchiveId == archive.archiveId) {
                                    shareItem = Record(item, archive, false)
                                    sharesByMe.add(shareItem)
                                } else {
                                    shareItem = Record(item, archive, true)
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
                error?.let { showMessage.value = it }
            }
        })
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnSharesByMeRetrieved(): LiveData<MutableList<Record>> = onSharesByMeRetrieved

    fun getOnSharesWithMeRetrieved(): LiveData<MutableList<Record>> = onSharesWithMeRetrieved
}