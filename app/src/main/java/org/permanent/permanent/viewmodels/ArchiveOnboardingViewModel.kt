package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Status
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.DefaultSelectionFragment
import org.permanent.permanent.ui.archiveOnboarding.NameSettingFragment
import org.permanent.permanent.ui.archiveOnboarding.OnboardingArchiveListener
import org.permanent.permanent.ui.archiveOnboarding.OnboardingPage
import org.permanent.permanent.ui.archiveOnboarding.PendingInvitationsFragment
import org.permanent.permanent.ui.archiveOnboarding.TypeSelectionFragment
import org.permanent.permanent.ui.archiveOnboarding.WelcomeFragment
import org.permanent.permanent.ui.archiveOnboarding.compose.NewArchive
import org.permanent.permanent.ui.archiveOnboarding.compose.OnboardingGoalType
import org.permanent.permanent.ui.archiveOnboarding.compose.OnboardingPriorityType

class ArchiveOnboardingViewModel(application: Application) :
    ObservableAndroidViewModel(application), OnboardingArchiveListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val showMessage = SingleLiveEvent<String>()
    private var accountName = MutableLiveData("")
    private lateinit var newArchive: NewArchive
    private var isTablet = false
    private val isArchiveSelected = MutableLiveData(false)
    private val selectedArchiveType = MutableLiveData<ArchiveType>()
    private val selectedArchiveTypeTitle = MutableLiveData<String>()
    private val selectedArchiveTypeText = MutableLiveData<String>()
    private val name = MutableLiveData<String>()
    private val onPendingArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onShowNextFragment = SingleLiveEvent<Fragment>()
    private val onArchiveOnboardingDone = SingleLiveEvent<Void?>()
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
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    private val _isFirstProgressBarEmpty = MutableStateFlow(false)
    val isFirstProgressBarEmpty: StateFlow<Boolean> = _isFirstProgressBarEmpty
    private val _isSecondProgressBarEmpty = MutableStateFlow(true)
    val isSecondProgressBarEmpty: StateFlow<Boolean> = _isSecondProgressBarEmpty
    private val _isThirdProgressBarEmpty = MutableStateFlow(true)
    val isThirdProgressBarEmpty: StateFlow<Boolean> = _isThirdProgressBarEmpty
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _showError = MutableStateFlow("")
    val showError: StateFlow<String> = _showError

    init {
        accountName.value = prefsHelper.getAccountName()
        isTablet = prefsHelper.isTablet()
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
                error?.let { _showError.value = it }
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

    fun updateFirstProgressBarEmpty(isEmpty: Boolean) {
        _isFirstProgressBarEmpty.update { isEmpty }
    }

    fun updateSecondProgressBarEmpty(isEmpty: Boolean) {
        _isSecondProgressBarEmpty.update { isEmpty }
    }

    fun updateThirdProgressBarEmpty(isEmpty: Boolean) {
        _isThirdProgressBarEmpty.update { isEmpty }
    }

    fun createOnboardingGoals(context: Context): List<Pair<Int, String>> {
        return listOf(
            OnboardingGoalType.CAPTURE.ordinal to context.getString(R.string.goals_capture),
            OnboardingGoalType.DIGITIZE.ordinal to context.getString(R.string.goals_digitize),
            OnboardingGoalType.COLLABORATE.ordinal to context.getString(R.string.goals_collaborate),
            OnboardingGoalType.PUBLISH.ordinal to context.getString(R.string.goals_publish),
            OnboardingGoalType.SHARE.ordinal to context.getString(R.string.goals_share),
            OnboardingGoalType.LEGACY.ordinal to context.getString(R.string.goals_legacy),
            OnboardingGoalType.ORGANIZE.ordinal to context.getString(R.string.goals_organize),
            OnboardingGoalType.UNDEFINED.ordinal to context.getString(R.string.goals_undefined)
        )
    }

    fun createOnboardingPriorities(context: Context): List<Pair<Int, String>> {
        return listOf(
            OnboardingPriorityType.SAFE.ordinal to context.getString(R.string.priorities_safe),
            OnboardingPriorityType.NONPROFIT.ordinal to context.getString(R.string.priorities_nonprofit),
            OnboardingPriorityType.GENEALOGY.ordinal to context.getString(R.string.priorities_genealogy),
            OnboardingPriorityType.PROFESSIONAL.ordinal to context.getString(R.string.priorities_professional),
            OnboardingPriorityType.COLLABORATE.ordinal to context.getString(R.string.priorities_collaborate),
            OnboardingPriorityType.DIGIPRES.ordinal to context.getString(R.string.priorities_digipres)
        )
    }

    fun createNewArchive(newArchive: NewArchive) {
        this.newArchive = newArchive
        _isBusyState.value = true
        archiveRepository.createNewArchive(
            newArchive.name,
            newArchive.type,
            object : IArchiveRepository.IArchiveListener {
                override fun onSuccess(archive: Archive) {
                    _isBusyState.value = false
                    setNewArchiveAsDefault(archive)
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let { _showError.value = it }
                }
            })
    }

    fun setNewArchiveAsDefault(archive: Archive) {
        val account = Account(prefsHelper.getAccountId(), prefsHelper.getAccountEmail())
        account.defaultArchiveId = archive.id

        _isBusyState.value = true
        accountRepository.update(account, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                prefsHelper.saveDefaultArchiveId(archive.id)
                setNewArchiveAsCurrent(archive)
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let { _showError.value = it }
            }
        })
    }

    fun setNewArchiveAsCurrent(archive: Archive) {
        archive.number?.let { archiveNr ->
            _isBusyState.value = true
            archiveRepository.switchToArchive(archiveNr, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    _isBusyState.value = false
                    prefsHelper.saveCurrentArchiveInfo(
                        archive.id,
                        archive.number,
                        archive.type,
                        archive.fullName,
                        archive.thumbURL200,
                        archive.accessRole
                    )
                    login()
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let { _showError.value = it }
                }
            })
        }
    }

    fun login() {
        val email = prefsHelper.getAccountEmail()
        val password = prefsHelper.getAccountPassword()

        if (email != null && password != null) {
            _isBusyState.value = true
            authRepository.login(email,
                password,
                object : IAuthenticationRepository.IOnLoginListener {
                    override fun onSuccess() {
                        _isBusyState.value = false
                        prefsHelper.saveUserLoggedIn(true)
                        sendGoalsAndPriorities()
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        when (error) {
                            Constants.ERROR_SERVER_ERROR -> _showError.value =
                                appContext.getString(R.string.server_error)

                            null -> _showError.value = appContext.getString(R.string.generic_error)

                            else -> _showError.value = error!!
                        }
                    }
                })
        }
    }

    fun sendGoalsAndPriorities() {
        val addTags = mutableListOf<String>()
        when (newArchive.typeName) {
            appContext.getString(R.string.personal) -> addTags.add("type:myself")
            appContext.getString(R.string.individual) -> addTags.add("type:individual")
            appContext.getString(R.string.family) -> addTags.add("type:family")
            appContext.getString(R.string.family_history) -> addTags.add("type:famhist")
            appContext.getString(R.string.community) -> addTags.add("type:community")
            appContext.getString(R.string.organization) -> addTags.add("type:org")
        }
        newArchive.goals.forEach { goal ->
            if (goal.isChecked.value) {
                addTags.add("goal:" + goal.type.name.lowercase())
            }
        }
        newArchive.priorities.forEach { priority ->
            if (priority.isChecked.value) {
                addTags.add("why:" + priority.type.name.lowercase())
            }
        }
        val tags = Tags(addTags = addTags, removeTags = listOf())
        _isBusyState.value = true
        stelaAccountRepository.addRemoveTags(tags, object : IResponseListener {

            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                // TODO: show congrats screen
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let { _showError.value = it }
            }
        })
    }

    //    fun onBackBtnClick() {
//        if (currentPage.value == OnboardingPage.TYPE_SELECTION) {
//            if (onPendingArchivesRetrieved.value?.isNotEmpty() == true) {
//                if (areAllArchivesAccepted) {
//                    showFragment(defaultSelectionFragment)
//                } else {
//                    showFragment(pendingInvitationsFragment)
//                }
//            } else {
//                showFragment(welcomeFragment)
//            }
//        } else {
//            showFragment(typeSelectionFragment)
//        }
//        progress.value = progress.value?.minus(1)
//    }
//
//    fun onCreateArchiveBtnClick() {
//        if (isBusy.value != null && isBusy.value!!) {
//            return
//        }
//        val name = name.value?.trim()
//        val selectedArchiveType = selectedArchiveType.value
//
//        if (name != null && selectedArchiveType != null) {
//            isBusy.value = true
//            archiveRepository.createNewArchive(
//                name,
//                selectedArchiveType,
//                object : IArchiveRepository.IArchiveListener {
//                    override fun onSuccess(archive: Archive) {
//                        isBusy.value = false
//                        setNewArchiveAsDefault(archive)
//                    }
//
//                    override fun onFailed(error: String?) {
//                        isBusy.value = false
//                        error?.let { showError.value = it }
//                    }
//                })
//        }
//    }
//
//    fun onCreateNewArchiveBtnClick() {
//        onGetStartedBtnClick()
//    }
//
    override fun onAcceptBtnClick(archive: Archive) {
//        if (isBusy.value != null && isBusy.value!!) {
//            return
//        }
//
//        isBusy.value = true
//        archiveRepository.acceptArchives(listOf(archive), object : IResponseListener {
//            override fun onSuccess(message: String?) {
//                isBusy.value = false
//                archive.status = Status.OK
//                setNewArchiveAsDefault(archive)
//            }
//
//            override fun onFailed(error: String?) {
//                isBusy.value = false
//                error?.let { showError.value = it }
//                return
//            }
//        })
    }

    //
//    fun onAcceptAllBtnClick() {
//        if (isBusy.value != null && isBusy.value!!) {
//            return
//        }
//
//        val pendingArchives = onPendingArchivesRetrieved.value
//
//        if (!pendingArchives.isNullOrEmpty()) {
//            isBusy.value = true
//            archiveRepository.acceptArchives(pendingArchives, object : IResponseListener {
//                override fun onSuccess(message: String?) {
//                    isBusy.value = false
//                    for (pendingArchive in pendingArchives) {
//                        pendingArchive.status = Status.OK
//                    }
//                    onPendingArchivesRetrieved.value = onPendingArchivesRetrieved.value
//                    showFragment(defaultSelectionFragment)
//                    confirmationText.value = appContext.getString(
//                        R.string.archive_onboarding_default_selection_text,
//                        pendingArchives.size.toString()
//                    )
//                    areAllArchivesAccepted = true
//                }
//
//                override fun onFailed(error: String?) {
//                    isBusy.value = false
//                    error?.let { showError.value = it }
//                    return
//                }
//            })
//        }
//    }
//
    override fun onMakeDefaultBtnClick(archive: Archive) {
//        setNewArchiveAsDefault(archive)
    }

    fun getShowMessage(): LiveData<String> = showMessage
    fun getAccountName() = accountName
    fun isTablet() = isTablet
    fun getCurrentPage(): MutableLiveData<OnboardingPage> = currentPage
    fun getIsArchiveSelected(): MutableLiveData<Boolean> = isArchiveSelected
    fun getSelectedArchiveType(): MutableLiveData<ArchiveType> = selectedArchiveType
    fun getSelectedArchiveTypeTitle(): MutableLiveData<String> = selectedArchiveTypeTitle
    fun getSelectedArchiveTypeText(): MutableLiveData<String> = selectedArchiveTypeText
    fun getName(): MutableLiveData<String> = name
    fun getConfirmationText(): MutableLiveData<String> = confirmationText
    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onPendingArchivesRetrieved
    fun getOnShowNextFragment(): LiveData<Fragment> = onShowNextFragment
    fun getOnArchiveOnboardingDone(): LiveData<Void?> = onArchiveOnboardingDone
}
