package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archives.PendingArchiveListener

class ArchivesViewModel(application: Application) : ObservableAndroidViewModel(application),
    PendingArchiveListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var showScreenSimplified = MutableLiveData(false)
    private val isCurrentArchiveDefault = MutableLiveData(false)
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val currentArchiveName =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveFullName())
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val existsPendingArchives = MutableLiveData(false)
    private val onPendingArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val existsArchives = MutableLiveData(false)
    private val onArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onDefaultArchiveChanged = MutableLiveData<Int>()
    private val onShowCreateArchiveDialog = SingleLiveEvent<Void>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    init {
        getDefaultArchive()
    }

    private fun getDefaultArchive() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                isBusy.value = false
                prefsHelper.saveDefaultArchiveId(account.defaultArchiveId)
                isCurrentArchiveDefault.value =
                    prefsHelper.getCurrentArchiveId() == account.defaultArchiveId
                refreshArchives()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun setShowScreenSimplified() {
        showScreenSimplified.value = true
    }

    fun refreshArchives() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val currentArchiveId = prefsHelper.getCurrentArchiveId()
                    val pendingArchives: MutableList<Archive> = ArrayList()
                    val archives: MutableList<Archive> = ArrayList()

                    for (datum in dataList) {
                        val archive = Archive(datum.ArchiveVO)
                        if (currentArchiveId != archive.id) {
                            if (archive.status == Status.PENDING) pendingArchives.add(archive) else
                                archives.add(archive)
                        }
                    }
                    existsPendingArchives.value = pendingArchives.isNotEmpty()
                    onPendingArchivesRetrieved.value = pendingArchives
                    existsArchives.value = archives.isNotEmpty()
                    onArchivesRetrieved.value = archives
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    override fun onAcceptBtnClick(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.acceptArchive(archive, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                refreshArchives()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    override fun onDeclineBtnClick(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.declineArchive(archive, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                refreshArchives()
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
        archive.number?.let {
            archiveRepository.switchToArchive(it, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    prefsHelper.saveCurrentArchiveInfo(
                        archive.id,
                        archive.number,
                        archive.fullName,
                        archive.thumbURL500,
                        archive.accessRole
                    )
                    refreshArchives()
                    isCurrentArchiveDefault.value = archive.id == prefsHelper.getDefaultArchiveId()
                    currentArchiveThumb.value = archive.thumbURL500
                    currentArchiveName.value = archive.fullName
                    showMessage.value = appContext.getString(R.string.archive_current_archive_switch_success)
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
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

    fun deleteArchive(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archive.number?.let {
            archiveRepository.deleteArchive(it, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                    refreshArchives()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }

    fun onCreateNewArchiveBtnClick() {
        onShowCreateArchiveDialog.call()
    }

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    fun getIsCurrentArchiveDefault(): MutableLiveData<Boolean> = isCurrentArchiveDefault

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getExistsPendingArchives(): MutableLiveData<Boolean> = existsPendingArchives

    fun getOnPendingArchivesRetrieved(): LiveData<List<Archive>> = onPendingArchivesRetrieved

    fun getExistsArchives(): MutableLiveData<Boolean> = existsArchives

    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onArchivesRetrieved

    fun getOnDefaultArchiveChanged(): LiveData<Int> = onDefaultArchiveChanged

    fun getShowCreateArchiveDialog(): LiveData<Void> = onShowCreateArchiveDialog
}