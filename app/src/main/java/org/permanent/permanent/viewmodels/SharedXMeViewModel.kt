package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Upload
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord

class SharedXMeViewModel(application: Application
) : ObservableAndroidViewModel(application), OnFinishedListener {

    private val appContext = application.applicationContext
    var existsShares = MutableLiveData(false)
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var downloadQueue: DownloadQueue
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        downloadQueue = DownloadQueue(appContext, lifecycleOwner, this)
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun download(record: DownloadableRecord) {
        val download = downloadQueue.enqueueNewDownloadFor(record)
        record.observe(lifecycleOwner, download)
    }

    override fun onFinished(download: Download) {
        showMessage.value = "Downloaded ${download.getDisplayName()}"
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) { // Not needed
    }
}