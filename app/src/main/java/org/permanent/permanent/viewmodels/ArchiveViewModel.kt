package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archives.ArchiveListener

class ArchiveViewModel(application: Application) : ObservableAndroidViewModel(application),
    ArchiveListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val currentArchiveThumb = prefsHelper.getArchiveThumbURL()
    private val currentArchiveName =
        application.getString(R.string.nav_main_header_title_text, prefsHelper.getArchiveFullName())
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val existsArchives = MutableLiveData(false)
    private val onArchivesRetrieved = MutableLiveData<List<Archive>>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    init {
        getArchives()
    }

    fun getArchives() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val currentArchiveId = prefsHelper.getArchiveId()
                    val archives: MutableList<Archive> = ArrayList()

                    for (datum in dataList) {
                        val archive = Archive(datum.ArchiveVO)
                        if (currentArchiveId != archive.id) {
                            archives.add(archive)
                        }
                    }
                    onArchivesRetrieved.value = archives
                    existsArchives.value = archives.isNotEmpty()
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    override fun onArchiveClick(archive: Archive) {
    }

    fun onCreateNewArchiveClick() {
    }

    fun getCurrentArchiveThumb(): String? = currentArchiveThumb

    fun getCurrentArchiveName(): String = currentArchiveName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getExistsArchives(): MutableLiveData<Boolean> = existsArchives

    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onArchivesRetrieved
}
