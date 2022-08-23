package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
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
import org.permanent.permanent.ui.archiveOnboarding.*

class ArchiveOnboardingViewModel(application: Application) :
    ObservableAndroidViewModel(application), OnboardingArchiveListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private val isArchiveSelected = MutableLiveData(false)
    private val selectedArchiveType = MutableLiveData<ArchiveType>()
    private val selectedArchiveTypeTitle = MutableLiveData<String>()
    private val selectedArchiveTypeText = MutableLiveData<String>()
    private val name = MutableLiveData<String>()
    private val onPendingArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onShowNextFragment = SingleLiveEvent<Fragment>()
    private val onArchiveOnboardingDone = SingleLiveEvent<Void>()
    private val currentPage = MutableLiveData(OnboardingPage.WELCOME)
    val progress = MutableLiveData(1)
    private val confirmationText = MutableLiveData<String>()
    private var welcomeFragment = WelcomeFragment()
    private var typeSelectionFragment = TypeSelectionFragment()
    private var nameSettingFragment = NameSettingFragment()
    private var pendingInvitationsFragment = PendingInvitationsFragment()
    private var defaultSelectionFragment = DefaultSelectionFragment()
    private var areAllArchivesAccepted = false
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    init {
        getPendingArchives()
    }

    private fun getPendingArchives() {
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                if (!dataList.isNullOrEmpty()) {
                    val pendingArchives: MutableList<Archive> = ArrayList()
                    for (data in dataList) {
                        val archive = Archive(data.ArchiveVO)
                        if (archive.status == Status.PENDING) pendingArchives.add(archive)
                    }
                    if (pendingArchives.isNotEmpty()) {
                        showFragment(pendingInvitationsFragment)
                        onPendingArchivesRetrieved.value = pendingArchives
                    } else {
                        showFragment(welcomeFragment)
                    }
                } else {
                    showFragment(welcomeFragment)
                }
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    private fun showFragment(fragment: Fragment) {
        when (fragment) {
            welcomeFragment -> {
                currentPage.value = OnboardingPage.WELCOME
                prefsHelper.saveArchiveOnboardingDefaultFlow(true)
            }
            typeSelectionFragment -> {
                currentPage.value = OnboardingPage.TYPE_SELECTION
                prefsHelper.saveArchiveOnboardingDefaultFlow(true)
            }
            nameSettingFragment -> {
                currentPage.value = OnboardingPage.NAME_SETTING
                prefsHelper.saveArchiveOnboardingDefaultFlow(true)
            }
            pendingInvitationsFragment -> {
                currentPage.value = OnboardingPage.PENDING_INVITATIONS
                prefsHelper.saveArchiveOnboardingDefaultFlow(false)
            }
            defaultSelectionFragment -> {
                currentPage.value = OnboardingPage.DEFAULT_SELECTION
                prefsHelper.saveArchiveOnboardingDefaultFlow(false)
            }
        }
        onShowNextFragment.value = fragment
    }

    fun onArchiveTypeBtnClick(archiveType: ArchiveType) {
        isArchiveSelected.value = true
        selectedArchiveType.value = archiveType
        selectedArchiveTypeTitle.value = when (archiveType) {
            ArchiveType.FAMILY -> appContext.getString(R.string.archive_onboarding_group_archive_title)
            ArchiveType.ORGANIZATION -> appContext.getString(R.string.archive_onboarding_organization_archive_title)
            else -> appContext.getString(R.string.archive_onboarding_person_archive_title)
        }
        selectedArchiveTypeText.value = when (archiveType) {
            ArchiveType.FAMILY -> appContext.getString(R.string.archive_onboarding_group_archive_text)
            ArchiveType.ORGANIZATION -> appContext.getString(R.string.archive_onboarding_organization_archive_text)
            else -> appContext.getString(R.string.archive_onboarding_person_archive_text)
        }
    }

    fun onNameTextChanged(name: Editable) {
        this.name.value = name.toString()
    }

    fun onGetStartedBtnClick() {
        showFragment(typeSelectionFragment)
        progress.value = progress.value?.plus(1)
    }

    fun onBackBtnClick() {
        if (currentPage.value == OnboardingPage.TYPE_SELECTION) {
            if (onPendingArchivesRetrieved.value?.isNotEmpty() == true) {
                if (areAllArchivesAccepted) {
                    showFragment(defaultSelectionFragment)
                } else {
                    showFragment(pendingInvitationsFragment)
                }
            } else {
                showFragment(welcomeFragment)
            }
        } else {
            showFragment(typeSelectionFragment)
        }
        progress.value = progress.value?.minus(1)
    }

    fun onNameArchiveBtnClick() {
        showFragment(nameSettingFragment)
        progress.value = progress.value?.plus(1)
    }

    fun onCreateArchiveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = name.value?.trim()
        val selectedArchiveType = selectedArchiveType.value

        if (name != null && selectedArchiveType != null) {
            isBusy.value = true
            archiveRepository.createNewArchive(
                name,
                selectedArchiveType,
                object : IArchiveRepository.IArchiveListener {
                    override fun onSuccess(archive: Archive) {
                        isBusy.value = false
                        setNewArchiveAsDefault(archive)
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showError.value = it }
                    }
                })
        }
    }

    fun setNewArchiveAsDefault(newArchive: Archive) {
        val account = Account(prefsHelper.getAccountId(), prefsHelper.getAccountEmail())
        account.defaultArchiveId = newArchive.id

        accountRepository.update(account, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                prefsHelper.saveDefaultArchiveId(newArchive.id)
                setNewArchiveAsCurrent(newArchive)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
            }
        })
    }

    fun setNewArchiveAsCurrent(newArchive: Archive) {
        newArchive.number?.let { archiveNr ->
            archiveRepository.switchToArchive(archiveNr, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    prefsHelper.saveCurrentArchiveInfo(
                        newArchive.id,
                        newArchive.number,
                        newArchive.type,
                        newArchive.fullName,
                        newArchive.thumbURL200,
                        newArchive.accessRole
                    )
                    onArchiveOnboardingDone.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun onCreateNewArchiveBtnClick() {
        onGetStartedBtnClick()
    }

    override fun onAcceptBtnClick(archive: Archive) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.acceptArchives(listOf(archive), object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                archive.status = Status.OK
                setNewArchiveAsDefault(archive)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
                return
            }
        })
    }

    fun onAcceptAllBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val pendingArchives = onPendingArchivesRetrieved.value

        if (!pendingArchives.isNullOrEmpty()) {
            isBusy.value = true
            archiveRepository.acceptArchives(pendingArchives, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    for (pendingArchive in pendingArchives) {
                        pendingArchive.status = Status.OK
                    }
                    onPendingArchivesRetrieved.value = onPendingArchivesRetrieved.value
                    showFragment(defaultSelectionFragment)
                    confirmationText.value = appContext.getString(
                        R.string.archive_onboarding_default_selection_text,
                        pendingArchives.size.toString()
                    )
                    areAllArchivesAccepted = true
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                    return
                }
            })
        }
    }

    override fun onMakeDefaultBtnClick(archive: Archive) {
        setNewArchiveAsDefault(archive)
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): LiveData<String> = showMessage
    fun getShowError(): LiveData<String> = showError

    fun getCurrentPage(): MutableLiveData<OnboardingPage> = currentPage
    fun getIsArchiveSelected(): MutableLiveData<Boolean> = isArchiveSelected
    fun getSelectedArchiveType(): MutableLiveData<ArchiveType> = selectedArchiveType
    fun getSelectedArchiveTypeTitle(): MutableLiveData<String> = selectedArchiveTypeTitle
    fun getSelectedArchiveTypeText(): MutableLiveData<String> = selectedArchiveTypeText
    fun getName(): MutableLiveData<String> = name
    fun getConfirmationText(): MutableLiveData<String> = confirmationText

    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onPendingArchivesRetrieved
    fun getOnShowNextFragment(): LiveData<Fragment> = onShowNextFragment
    fun getOnArchiveOnboardingDone(): LiveData<Void> = onArchiveOnboardingDone
}
