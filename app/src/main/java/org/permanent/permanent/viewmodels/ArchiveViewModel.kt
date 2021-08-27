package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class ArchiveViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isCurrentArchiveDefault =
        MutableLiveData(prefsHelper.getCurrentArchiveId() == prefsHelper.getDefaultArchiveId())
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val currentArchiveName =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveFullName())
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val existsArchives = MutableLiveData(false)
    private val onArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onDefaultArchiveChanged = MutableLiveData<Int>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

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
                    val currentArchiveId = prefsHelper.getCurrentArchiveId()
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

    fun switchCurrentArchiveTo(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.switchToArchive(archive.number, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                prefsHelper.saveCurrentArchiveInfo(
                    archive.id,
                    archive.number,
                    archive.fullName,
                    archive.thumbURL500
                )
                getArchives()
                isCurrentArchiveDefault.value = archive.id == prefsHelper.getDefaultArchiveId()
                currentArchiveThumb.value = archive.thumbURL500
                currentArchiveName.value = archive.fullName
                showMessage.value = message
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun changeDefaultArchiveTo(newDefaultArchiveId: Int) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        accountRepository.changeDefaultArchive(newDefaultArchiveId, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                prefsHelper.saveDefaultArchiveId(newDefaultArchiveId)
                val currentArchiveId = prefsHelper.getCurrentArchiveId()

                if (currentArchiveId == newDefaultArchiveId) {
                    isCurrentArchiveDefault.value = true
                    onDefaultArchiveChanged.value = newDefaultArchiveId
                } else {
                    isCurrentArchiveDefault.value = false
                    onDefaultArchiveChanged.value = newDefaultArchiveId
                }
                showMessage.value = message
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun onCreateNewArchiveBtnClick() {
    }

    fun getIsCurrentArchiveDefault(): MutableLiveData<Boolean> = isCurrentArchiveDefault

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getExistsArchives(): MutableLiveData<Boolean> = existsArchives

    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onArchivesRetrieved

    fun getOnDefaultArchiveChanged(): LiveData<Int> = onDefaultArchiveChanged
}
