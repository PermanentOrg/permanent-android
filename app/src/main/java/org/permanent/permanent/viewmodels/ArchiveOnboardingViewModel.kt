package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
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
import org.permanent.permanent.ui.archiveOnboarding.OnboardingArchiveListener
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
    private val selectedArchiveType = MutableLiveData<ArchiveType>()
    private val selectedArchiveTypeTitle = MutableLiveData<String>()
    private val selectedArchiveTypeText = MutableLiveData<String>()
    private val name = MutableLiveData<String>()
    private val onPendingArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onArchiveOnboardingDone = SingleLiveEvent<Void?>()
    val progress = MutableLiveData(1)
    private val confirmationText = MutableLiveData<String>()
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
    private val _newArchiveCallsSuccess = MutableStateFlow(false)
    val newArchiveCallsSuccess: StateFlow<Boolean> = _newArchiveCallsSuccess
    private val _isAcceptedArchiveFlow = MutableStateFlow(false)
    val isAcceptedArchiveFlow: StateFlow<Boolean> = _isAcceptedArchiveFlow
    private val _isArchiveCreationFlow = MutableStateFlow(false)
    val isArchiveCreationFlow: StateFlow<Boolean> = _isArchiveCreationFlow
    private val _acceptedArchives = MutableStateFlow<MutableList<Archive>>(mutableListOf())
    val acceptedArchives: StateFlow<List<Archive>> = _acceptedArchives
    private val _allArchives = MutableStateFlow<MutableList<Archive>>(mutableListOf())
    val allArchives: StateFlow<List<Archive>> = _allArchives

    init {
        accountName.value = prefsHelper.getAccountName()
        isTablet = prefsHelper.isTablet()
        getAllArchives()
    }

    private fun getAllArchives() {
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                val acceptedArchives: MutableList<Archive> = ArrayList()
                val allArchives: MutableList<Archive> = ArrayList()
                if (!dataList.isNullOrEmpty()) {
                    for (data in dataList) {
                        val archive = Archive(data.ArchiveVO)
                        if (archive.status == Status.OK) {
                            acceptedArchives.add(archive)
                        }
                        allArchives.add(archive)
                    }
                }
                // Move the archive with AccessRole.OWNER to the first position
                acceptedArchives.sortByDescending { it.accessRole == AccessRole.OWNER }

                _acceptedArchives.value = acceptedArchives
                _allArchives.value = allArchives
            }

            override fun onFailed(error: String?) {
                error?.let { _showError.value = it }
            }
        })
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
            OnboardingPriorityType.GENEALOGY.ordinal to context.getString(R.string.priorities_genealogy),
            OnboardingPriorityType.NONPROFIT.ordinal to context.getString(R.string.priorities_nonprofit),
            OnboardingPriorityType.SAFE.ordinal to context.getString(R.string.priorities_safe),
            OnboardingPriorityType.PROFESSIONAL.ordinal to context.getString(R.string.priorities_professional),
            OnboardingPriorityType.COLLABORATE.ordinal to context.getString(R.string.priorities_collaborate),
            OnboardingPriorityType.DIGIPRES.ordinal to context.getString(R.string.priorities_digipres)
        )
    }

    fun onNextButtonClick(newArchive: NewArchive) {
        if (_isAcceptedArchiveFlow.value) {
            getAllArchives()
            this.newArchive.goals = newArchive.goals
            this.newArchive.priorities = newArchive.priorities
            sendGoalsAndPriorities()
        } else {
            createNewArchive(newArchive)
        }
    }

    private fun createNewArchive(newArchive: NewArchive) {
        this.newArchive = newArchive
        _isArchiveCreationFlow.value = true
        _isBusyState.value = true

        archiveRepository.createNewArchive(newArchive.name,
            newArchive.type,
            object : IArchiveRepository.IArchiveListener {
                override fun onSuccess(archive: Archive) {
                    _isBusyState.value = false
                    _acceptedArchives.value.add(0, archive)
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
                    if (_isArchiveCreationFlow.value) getAllArchives()
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
            authRepository.login(
                email,
                password,
                object : IAuthenticationRepository.IOnLoginListener {
                    override fun onSuccess() {
                        _isBusyState.value = false
                        prefsHelper.saveUserLoggedIn(true)
                        prefsHelper.setIsTwoFAEnabled(false)
                        if (_isArchiveCreationFlow.value) sendGoalsAndPriorities()
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        when (error) {
                            Constants.ERROR_SERVER_ERROR -> _showError.value =
                                appContext.getString(R.string.server_error)

                            null -> _showError.value = appContext.getString(R.string.generic_error)

                            else -> _showError.value = error
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
                _newArchiveCallsSuccess.value = true
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                _newArchiveCallsSuccess.value = true
                error?.let { _showError.value = it }
            }
        })
    }

    override fun onAcceptBtnClick(archive: Archive) {
        if (_isBusyState.value) {
            return
        }
        this.newArchive = NewArchive(
            type = archive.type ?: ArchiveType.PERSON,
            typeName = when (archive.type) {
                ArchiveType.PERSON -> appContext.getString(R.string.personal)
                ArchiveType.FAMILY -> appContext.getString(R.string.family)
                ArchiveType.ORGANIZATION -> appContext.getString(R.string.organization)
                ArchiveType.NONPROFIT -> appContext.getString(R.string.organization)
                else -> appContext.getString(R.string.personal)
            },
            name = "",
            goals = mutableStateListOf(),
            priorities = mutableStateListOf()
        )

        _isArchiveCreationFlow.value = false
        _isBusyState.value = true
        archiveRepository.acceptArchives(listOf(archive), object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                archive.status = Status.OK
                setNewArchiveAsDefault(archive)
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let { _showError.value = it }
                return
            }
        })
    }

    fun resetNewArchiveCallsSuccess() {
        _newArchiveCallsSuccess.value = false
    }

    fun setAcceptedArchiveFlow() {
        _isAcceptedArchiveFlow.value = true
    }

    fun completeArchiveOnboarding() {
        onArchiveOnboardingDone.call()
    }

    override fun onMakeDefaultBtnClick(archive: Archive) {
//        setNewArchiveAsDefault(archive)
    }

    fun getShowMessage(): LiveData<String> = showMessage
    fun getAccountName() = accountName
    fun isTablet() = isTablet
    fun getSelectedArchiveType(): MutableLiveData<ArchiveType> = selectedArchiveType
    fun getSelectedArchiveTypeTitle(): MutableLiveData<String> = selectedArchiveTypeTitle
    fun getSelectedArchiveTypeText(): MutableLiveData<String> = selectedArchiveTypeText
    fun getName(): MutableLiveData<String> = name
    fun getConfirmationText(): MutableLiveData<String> = confirmationText
    fun getOnArchivesRetrieved(): LiveData<List<Archive>> = onPendingArchivesRetrieved
    fun getOnArchiveOnboardingDone(): LiveData<Void?> = onArchiveOnboardingDone
}
