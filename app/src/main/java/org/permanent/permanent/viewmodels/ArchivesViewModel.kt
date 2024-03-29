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
    private val showError = MutableLiveData<String>()
    private val existsPendingArchives = MutableLiveData(false)
    private val onPendingArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val existsArchives = MutableLiveData(false)
    private val onArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onDefaultArchiveChanged = MutableLiveData<Int>()
    private val onCurrentArchiveChanged = SingleLiveEvent<Void?>()
    private val onShowCreateArchiveDialog = SingleLiveEvent<Void?>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    init {
        // This is needed in case the default changes on the web and the user is already logged in
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
                error?.let { errorMsg -> showError.value = errorMsg }
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
                        } else {
                            updateCurrentArchive(archive)
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
                error?.let { errorMsg -> showError.value = errorMsg }
            }
        })
    }

    private fun updateCurrentArchive(archive: Archive) {
        prefsHelper.saveCurrentArchiveInfo(
            archive.id,
            archive.number,
            archive.type,
            archive.fullName,
            archive.thumbURL200,
            archive.accessRole
        )
    }

    override fun onAcceptBtnClick(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.acceptArchives(listOf(archive), object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                message?.let { showMessage.value = it }
                refreshArchives()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
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
                message?.let { showMessage.value = it }
                refreshArchives()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { errorMsg -> showError.value = errorMsg }
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
                        archive.type,
                        archive.fullName,
                        archive.thumbURL200,
                        archive.accessRole
                    )
                    refreshArchives()
                    isCurrentArchiveDefault.value = archive.id == prefsHelper.getDefaultArchiveId()
                    currentArchiveThumb.value = archive.thumbURL200
                    currentArchiveName.value = archive.fullName
                    showMessage.value = appContext.getString(R.string.archive_current_archive_switch_success)
                    if (showScreenSimplified.value == true) onCurrentArchiveChanged.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMsg -> showError.value = errorMsg }
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
                message?.let { showMessage.value = it }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { errorMsg -> showError.value = errorMsg }
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
                    message?.let { showMessage.value = it }
                    refreshArchives()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMsg -> showError.value = errorMsg }
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
    fun getShowError(): LiveData<String> = showError

    fun getExistsPendingArchives(): MutableLiveData<Boolean> = existsPendingArchives

    fun getOnPendingArchivesRetrieved(): LiveData<List<Archive>> = onPendingArchivesRetrieved

    fun getExistsArchives(): MutableLiveData<Boolean> = existsArchives

    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onArchivesRetrieved

    fun getOnDefaultArchiveChanged(): LiveData<Int> = onDefaultArchiveChanged

    fun getOnCurrentArchiveChanged(): SingleLiveEvent<Void?> = onCurrentArchiveChanged

    fun getShowCreateArchiveDialog(): SingleLiveEvent<Void?> = onShowCreateArchiveDialog
}
