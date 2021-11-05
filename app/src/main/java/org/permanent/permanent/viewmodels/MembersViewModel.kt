package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.members.MemberListener

class MembersViewModel(application: Application
) : ObservableAndroidViewModel(application), MemberListener {

    private val appContext = application.applicationContext
    private val ownerName = MutableLiveData<String>()
    private val ownerEmail = MutableLiveData<String>()
    private var pendingOwner = MutableLiveData<Account>()
    private val pendingOwnerName = MutableLiveData<String>()
    private val pendingOwnerEmail = MutableLiveData<String>()
    private val existsManagers = MutableLiveData(false)
    private val existsCurators = MutableLiveData(false)
    private val existsEditors = MutableLiveData(false)
    private val existsContributors = MutableLiveData(false)
    private val existsViewers = MutableLiveData(false)
    private val onManagersRetrieved = MutableLiveData<List<Account>>()
    private val onCuratorsRetrieved = MutableLiveData<List<Account>>()
    private val onEditorsRetrieved = MutableLiveData<List<Account>>()
    private val onContributorsRetrieved = MutableLiveData<List<Account>>()
    private val onViewersRetrieved = MutableLiveData<List<Account>>()
    private val isBusy = MutableLiveData(false)
    private val isArchiveShareAvailable =
        CurrentArchivePermissionsManager.instance.isArchiveShareAvailable()
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarLong = MutableLiveData<Int>()
    private val showAddMemberDialogRequest = SingleLiveEvent<Void>()
    private val showMemberOptionsFragmentRequest = MutableLiveData<Account>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(appContext)

    init {
        refreshMembers()
    }

    fun refreshMembers() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.getMembers(object : IDataListener {

            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) splitByRole(dataList)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showSnackbar.value = error
            }
        })
    }

    private fun splitByRole(allMembers: List<Datum>) {
        pendingOwner.value = null
        val managers: MutableList<Account> = ArrayList()
        val curators: MutableList<Account> = ArrayList()
        val editors: MutableList<Account> = ArrayList()
        val contributors: MutableList<Account> = ArrayList()
        val viewers: MutableList<Account> = ArrayList()

        for (datum in allMembers) {
            val account = Account(datum.AccountVO)
            when (account.accessRole) {
                AccessRole.OWNER -> {
                    if (account.status == Status.PENDING) {
                        pendingOwner.value = account
                        pendingOwnerName.value = account.fullName
                        pendingOwnerEmail.value = account.primaryEmail
                    } else {
                        ownerName.value = account.fullName
                        ownerEmail.value = account.primaryEmail
                    }
                }
                AccessRole.MANAGER -> managers.add(account)
                AccessRole.CURATOR -> curators.add(account)
                AccessRole.EDITOR -> editors.add(account)
                AccessRole.CONTRIBUTOR -> contributors.add(account)
                else -> viewers.add(account)
            }
        }
        onManagersRetrieved.value = managers
        onCuratorsRetrieved.value = curators
        onEditorsRetrieved.value = editors
        onContributorsRetrieved.value = contributors
        onViewersRetrieved.value = viewers
        existsManagers.value = managers.isNotEmpty()
        existsCurators.value = curators.isNotEmpty()
        existsEditors.value = editors.isNotEmpty()
        existsContributors.value = contributors.isNotEmpty()
        existsViewers.value = viewers.isNotEmpty()
    }

    fun onInfoBtnClick(stringInt: Int) {
        showSnackbarLong.value = stringInt
    }

    fun onAddMembersClick() {
        showAddMemberDialogRequest.call()
    }

    fun onPendingOwnerOptionsBtnClick() {
        showMemberOptionsFragmentRequest.value = pendingOwner.value
    }

    override fun onMemberOptionsClick(member: Account) {
        showMemberOptionsFragmentRequest.value = member
    }

    fun getOnManagersRetrieved(): LiveData<List<Account>> = onManagersRetrieved

    fun getOnCuratorsRetrieved(): LiveData<List<Account>> = onCuratorsRetrieved

    fun getOnEditorsRetrieved(): LiveData<List<Account>> = onEditorsRetrieved

    fun getOnContributorsRetrieved(): LiveData<List<Account>> = onContributorsRetrieved

    fun getOnViewersRetrieved(): LiveData<List<Account>> = onViewersRetrieved

    fun getOwnerName(): MutableLiveData<String> = ownerName

    fun getOwnerEmail(): MutableLiveData<String> = ownerEmail

    fun getPendingOwner(): MutableLiveData<Account> = pendingOwner

    fun getPendingOwnerName(): MutableLiveData<String> = pendingOwnerName

    fun getPendingOwnerEmail(): MutableLiveData<String> = pendingOwnerEmail

    fun getExistsManagers(): MutableLiveData<Boolean> = existsManagers

    fun getExistsCurators(): MutableLiveData<Boolean> = existsCurators

    fun getExistsEditors(): MutableLiveData<Boolean> = existsEditors

    fun getExistsContributors(): MutableLiveData<Boolean> = existsContributors

    fun getExistsViewers(): MutableLiveData<Boolean> = existsViewers

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getIsArchiveShareAvailable(): Boolean = isArchiveShareAvailable

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowAddMemberDialogRequest(): LiveData<Void> = showAddMemberDialogRequest

    fun getShowMemberOptionsFragmentRequest(): LiveData<Account> = showMemberOptionsFragmentRequest

    fun getShowSnackbarLong(): LiveData<Int> = showSnackbarLong
}