package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicGalleryViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private val onPublicArchivesRetrieved = MutableLiveData<List<Archive>>()

    fun getYourPublicArchives() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        with(archiveRepository) {
            getAllArchives(object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        val archives: MutableList<Archive> = ArrayList()
                        for (datum in dataList) {
                            val archive = Archive(datum.ArchiveVO)
                            if (archive.public == 1) {
                                archives.add(archive)
                            }
                        }
                        onPublicArchivesRetrieved.value = archives
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showError.value = error
                }
            })
        }
    }

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): MutableLiveData<String> = showMessage
    fun getShowError(): MutableLiveData<String> = showError

    fun getOnPublicArchivesRetrieved(): LiveData<List<Archive>> = onPublicArchivesRetrieved

    fun sharePublicArchive(archive: Archive) {
        val sharableLink = BuildConfig.BASE_URL + "p/archive/" + archive.number +
                "/profile"
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_link_share_link_title), sharableLink
        )
        clipboard.setPrimaryClip(clip)
        showMessage.value = appContext.getString(R.string.share_link_link_copied)
    }

}