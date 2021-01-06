package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IMemberRepository
import org.permanent.permanent.repositories.MemberRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.members.MemberListener

class MembersViewModel(
    application: Application
) : ObservableAndroidViewModel(application), MemberListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val ownerDisplayName = prefsHelper.getUserFullName()
    private val ownerEmail = prefsHelper.getEmail()
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
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarLong = MutableLiveData<Int>()
    private val onShowAddMemberDialogRequest = MutableLiveData<Void>()
    private val onShowEditMemberDialogRequest = MutableLiveData<Account>()
    private var memberRepository: IMemberRepository = MemberRepositoryImpl(appContext)

    init {
        refreshMembers()
    }

    fun refreshMembers() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        memberRepository.getMembers(object : IDataListener {

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
        val managers: MutableList<Account> = ArrayList()
        val curators: MutableList<Account> = ArrayList()
        val editors: MutableList<Account> = ArrayList()
        val contributors: MutableList<Account> = ArrayList()
        val viewers: MutableList<Account> = ArrayList()
        val userAccountId = PreferencesHelper(
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ).getUserAccountId()

        for (datum in allMembers) {
            val account = Account(datum.AccountVO)
            if (userAccountId != account.id) {
                when (account.accessRole) {
                    AccessRole.MANAGER -> managers.add(account)
                    AccessRole.CURATOR -> curators.add(account)
                    AccessRole.EDITOR -> editors.add(account)
                    AccessRole.CONTRIBUTOR -> contributors.add(account)
                    else -> viewers.add(account)
                }
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

    fun getOnManagersRetrieved(): LiveData<List<Account>> {
        return onManagersRetrieved
    }

    fun getOnCuratorsRetrieved(): LiveData<List<Account>> {
        return onCuratorsRetrieved
    }

    fun getOnEditorsRetrieved(): LiveData<List<Account>> {
        return onEditorsRetrieved
    }

    fun getOnContributorsRetrieved(): LiveData<List<Account>> {
        return onContributorsRetrieved
    }

    fun getOnViewersRetrieved(): LiveData<List<Account>> {
        return onViewersRetrieved
    }

    fun getOwnerDisplayName(): String? {
        return ownerDisplayName
    }

    fun getOwnerEmail(): String? {
        return ownerEmail
    }

    fun getExistsManagers(): MutableLiveData<Boolean> {
        return existsManagers
    }

    fun getExistsCurators(): MutableLiveData<Boolean> {
        return existsCurators
    }

    fun getExistsEditors(): MutableLiveData<Boolean> {
        return existsEditors
    }

    fun getExistsContributors(): MutableLiveData<Boolean> {
        return existsContributors
    }

    fun getExistsViewers(): MutableLiveData<Boolean> {
        return existsViewers
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
    }

    fun getShowAddMemberDialogRequest(): LiveData<Void> {
        return onShowAddMemberDialogRequest
    }

    fun getShowEditMemberDialogRequest(): LiveData<Account> {
        return onShowEditMemberDialogRequest
    }

    fun getShowSnackbarLong(): LiveData<Int> {
        return showSnackbarLong
    }

    fun onInfoBtnClick(stringInt: Int) {
        showSnackbarLong.value = stringInt
    }

    fun onAddFabClick() {
        onShowAddMemberDialogRequest.value = onShowAddMemberDialogRequest.value
    }

    override fun onMemberEdit(member: Account) {
        onShowEditMemberDialogRequest.value = member
    }
}