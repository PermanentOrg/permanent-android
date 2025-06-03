package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.ChecklistItem
import org.permanent.permanent.network.models.IChecklistListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.composeComponents.SnackbarType

class ChecklistViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage
    private val _snackbarType = MutableStateFlow(SnackbarType.NONE)
    val snackbarType: StateFlow<SnackbarType> = _snackbarType
    private val _checklistItems = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val checklistItems: StateFlow<List<ChecklistItem>> = _checklistItems

    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    private val checklistIconMap = mapOf(
        "archiveCreated" to R.drawable.ic_archives_blue,
        "storageRedeemed" to R.drawable.ic_gift_blue_light,
        "firstUpload" to R.drawable.ic_file_upload_blue_light,
        "archiveSteward" to R.drawable.ic_archive_steward_blue,
        "legacyContact" to R.drawable.ic_legacy_contact_blue,
        "archiveProfile" to R.drawable.ic_archive_profile_blue,
        "publishContent" to R.drawable.ic_public_blue
    )

    init {
        getChecklist()
    }

    private fun getChecklist() {
        _isBusyState.value = true
        eventsRepository.getCheckList(object : IChecklistListener {

            override fun onSuccess(checklistList: List<ChecklistItem>) {
                val updatedList = checklistList.map {
                    if (it.id == "archiveCreated") it.copy(completed = true) else it
                }.sortedByDescending { it.completed }

                _checklistItems.value = updatedList
                _isBusyState.value = false
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                _snackbarMessage.value = error ?: appContext.getString(R.string.generic_error)
                _snackbarType.value = SnackbarType.ERROR
            }
        })
    }

    fun getIconForItem(id: String): Int =
        checklistIconMap[id] ?: R.drawable.ic_archives_blue

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun onChecklistItemClicked(item: ChecklistItem) {

    }

    fun dismissForeverChecklist(onDismiss: () -> Unit) {
        val accountId = prefsHelper.getAccountId()
        val email = prefsHelper.getAccountEmail()
        val account = Account(accountId, email)
        account.hideChecklist = true

        _isBusyState.value = true
        accountRepository.update(account, object : IResponseListener {

            override fun onSuccess(message: String?) {
                prefsHelper.saveAccountHideChecklist(true)
                _isBusyState.value = false
                onDismiss()
            }

            override fun onFailed(error: String?) {
                _snackbarMessage.value = error ?: appContext.getString(R.string.generic_error)
                _snackbarType.value = SnackbarType.ERROR
                _isBusyState.value = false
            }
        })
    }

    fun getAccountHideChecklist() = prefsHelper.getAccountHideChecklist()
}