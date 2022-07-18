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
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.NameSettingFragment
import org.permanent.permanent.ui.archiveOnboarding.OnboardingPage
import org.permanent.permanent.ui.archiveOnboarding.StartFragment
import org.permanent.permanent.ui.archiveOnboarding.TypeSelectionFragment

class ArchiveOnboardingViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

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
    private val onNextFragmentRequired = SingleLiveEvent<Fragment>()
    private val onArchiveCreated = SingleLiveEvent<Void>()
    val currentPage = MutableLiveData(OnboardingPage.START)
    val progress = MutableLiveData(1)
    private var startFragment = StartFragment()
    private var typeSelectionFragment = TypeSelectionFragment()
    private var nameSettingFragment = NameSettingFragment()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

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
        onNextFragmentRequired.value = typeSelectionFragment
        currentPage.value = OnboardingPage.TYPE_SELECTION
        progress.value = progress.value?.plus(1)
    }

    fun onBackBtnClick() {
        if (currentPage.value == OnboardingPage.TYPE_SELECTION) {
            onNextFragmentRequired.value = startFragment
            currentPage.value = OnboardingPage.START
        } else {
            onNextFragmentRequired.value = typeSelectionFragment
            currentPage.value = OnboardingPage.TYPE_SELECTION
        }
        progress.value = progress.value?.minus(1)
    }

    fun onNameArchiveBtnClick() {
        onNextFragmentRequired.value = nameSettingFragment
        currentPage.value = OnboardingPage.NAME_SETTING
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
                    onArchiveCreated.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): LiveData<String> = showMessage
    fun getShowError(): LiveData<String> = showError

    fun getIsArchiveSelected(): MutableLiveData<Boolean> = isArchiveSelected
    fun getSelectedArchiveType(): MutableLiveData<ArchiveType> = selectedArchiveType
    fun getSelectedArchiveTypeTitle(): MutableLiveData<String> = selectedArchiveTypeTitle
    fun getSelectedArchiveTypeText(): MutableLiveData<String> = selectedArchiveTypeText
    fun getName(): MutableLiveData<String> = name

    fun getOnNextFragmentRequired(): LiveData<Fragment> = onNextFragmentRequired
    fun getOnArchiveCreated(): LiveData<Void> = onArchiveCreated
}
