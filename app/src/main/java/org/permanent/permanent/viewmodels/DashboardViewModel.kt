package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.permanent.permanent.R
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.dashboard.CreateArchiveState
import org.permanent.permanent.ui.dashboard.DashboardWidgetType
import org.permanent.permanent.ui.dashboard.WidgetActionState
import org.permanent.permanent.ui.dashboard.defaultDashboardWidgets

/**
 * Backs the widget-based Dashboard shown to users who have no archive yet.
 *
 * The Create Archive widget is wired end-to-end: it reuses the exact repository chain the
 * onboarding flow uses (see [ArchiveOnboardingViewModel.createNewArchive]) so the created
 * archive is made default + current and prefs are left in the correct state.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private val accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private val stelaAccountRepository: StelaAccountRepository = StelaAccountRepositoryImpl(application)

    // Ordered widget inventory the screen renders. Swap/reorder here to experiment.
    val widgets: List<DashboardWidgetType> = defaultDashboardWidgets

    // Only show the loading skeleton when we genuinely have nothing to render yet — i.e. the
    // greeting's account name isn't cached. When it is (the normal case), the widgets render
    // straight from prefs and the skeleton is skipped (no flash on every entry).
    private val _isLoading = MutableStateFlow(prefsHelper.getAccountName().isNullOrBlank())
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _firstName = MutableStateFlow(extractFirstName(prefsHelper.getAccountName()))
    val firstName: StateFlow<String> = _firstName

    private val _createArchiveState = MutableStateFlow<CreateArchiveState>(CreateArchiveState.Idle)
    val createArchiveState: StateFlow<CreateArchiveState> = _createArchiveState

    private val _showError = MutableStateFlow("")
    val showError: StateFlow<String> = _showError

    // Confirmation shown (in the dashboard snackbar) after a successful tag save.
    private val _savedMessage = MutableStateFlow("")
    val savedMessage: StateFlow<String> = _savedMessage

    // Non-error guidance (e.g. "select at least one option") — shown as a warning snackbar.
    private val _warningMessage = MutableStateFlow("")
    val warningMessage: StateFlow<String> = _warningMessage

    // "What's important to you?" (why:* tags) and "Chart your path to success" (goal:* tags).
    // Both go Idle → Saving → Done; once Done the screen stops rendering the widget.
    private val _prioritiesState = MutableStateFlow(WidgetActionState.Idle)
    val prioritiesState: StateFlow<WidgetActionState> = _prioritiesState

    private val _chartPathState = MutableStateFlow(WidgetActionState.Idle)
    val chartPathState: StateFlow<WidgetActionState> = _chartPathState

    init {
        refresh()
    }

    /** Persists selected priorities as `why:*` account tags, then dismisses the widget. */
    fun savePriorities(tags: List<String>) =
        saveTags(tags, _prioritiesState, R.string.dashboard_priorities_saved)

    /** Persists selected goals as `goal:*` account tags, then dismisses the widget. */
    fun saveGoals(tags: List<String>) =
        saveTags(tags, _chartPathState, R.string.dashboard_goals_saved)

    // Reset once shown so the same message can be surfaced again (e.g. tapping Save with nothing
    // selected repeatedly still re-triggers the snackbar).
    fun onErrorShown() { _showError.value = "" }
    fun onSavedMessageShown() { _savedMessage.value = "" }
    fun onWarningShown() { _warningMessage.value = "" }

    // STUB: "Remind me later" has no persistence endpoint, so dismissal is session-local —
    // the widget reappears on the next cold start. A real contract would store a per-user
    // dismissal flag (server-side or in prefs).
    fun dismissPriorities() { _prioritiesState.value = WidgetActionState.Done }
    fun dismissGoals() { _chartPathState.value = WidgetActionState.Done }

    private fun saveTags(
        tags: List<String>,
        state: MutableStateFlow<WidgetActionState>,
        savedMessageRes: Int
    ) {
        if (state.value == WidgetActionState.Saving) return
        // Nothing selected: surface a warning and keep the widget visible (no dismiss, no call).
        if (tags.isEmpty()) {
            _warningMessage.value = getApplication<Application>().getString(R.string.dashboard_nothing_selected)
            return
        }
        state.value = WidgetActionState.Saving
        stelaAccountRepository.addRemoveTags(
            Tags(addTags = tags, removeTags = listOf()),
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    _savedMessage.value = getApplication<Application>().getString(savedMessageRes)
                    state.value = WidgetActionState.Done
                }

                // Dismiss either way so the widget never gets stuck; surface the error.
                override fun onFailed(error: String?) {
                    error?.let { _showError.value = it }
                    state.value = WidgetActionState.Done
                }
            })
    }

    /** Silently re-confirms account/archive state; only clears the skeleton if a cold load showed it. */
    fun refresh() {
        // Don't force the skeleton on — the widgets already render from cached prefs. This is a
        // background re-confirm. STUB: a real dashboard would aggregate per-widget data here
        // (recent activity, storage, suggestions) and show the skeleton only while that's pending.
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                _firstName.value = extractFirstName(prefsHelper.getAccountName())
                _isLoading.value = false
            }

            override fun onFailed(error: String?) {
                _isLoading.value = false
            }
        })
    }

    /**
     * Creates the user's first archive. Reuses [ArchiveRepository.createNewArchive] and then
     * mirrors the onboarding flow's "set as default → switch to current" steps so the rest of
     * the app (My Files, archive switcher) immediately reflects the new archive.
     */
    fun createArchive(name: String, type: ArchiveType) {
        if (name.isBlank() || _createArchiveState.value == CreateArchiveState.Creating) return

        _createArchiveState.value = CreateArchiveState.Creating
        archiveRepository.createNewArchive(
            name.trim(),
            type,
            object : IArchiveRepository.IArchiveListener {
                override fun onSuccess(archive: Archive) {
                    setNewArchiveAsDefault(archive)
                }

                override fun onFailed(error: String?) {
                    _createArchiveState.value = CreateArchiveState.Idle
                    error?.let { _showError.value = it }
                }
            })
    }

    private fun setNewArchiveAsDefault(archive: Archive) {
        val account = Account(prefsHelper.getAccountId(), prefsHelper.getAccountEmail())
        account.defaultArchiveId = archive.id

        accountRepository.update(account, object : IResponseListener {
            override fun onSuccess(message: String?) {
                prefsHelper.saveDefaultArchiveId(archive.id)
                setNewArchiveAsCurrent(archive)
            }

            override fun onFailed(error: String?) {
                _createArchiveState.value = CreateArchiveState.Idle
                error?.let { _showError.value = it }
            }
        })
    }

    private fun setNewArchiveAsCurrent(archive: Archive) {
        val archiveNr = archive.number
        if (archiveNr == null) {
            // Best-effort: the archive exists; we just couldn't switch to it. Treat as success.
            finishCreation(archive)
            return
        }
        archiveRepository.switchToArchive(archiveNr, object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                prefsHelper.saveCurrentArchiveInfo(
                    archive.id,
                    archive.number,
                    archive.type,
                    archive.fullName,
                    archive.thumbnail256 ?: archive.thumbURL200,
                    archive.accessRole
                )
                finishCreation(archive)
            }

            override fun onFailed(error: String?) {
                _createArchiveState.value = CreateArchiveState.Idle
                error?.let { _showError.value = it }
            }
        })
    }

    private fun finishCreation(archive: Archive) {
        // The user authenticated at sign-up; mark logged-in so a cold reopen lands in the app
        // (mirrors what the onboarding flow does once the first archive exists).
        prefsHelper.saveUserLoggedIn(true)
        _createArchiveState.value = CreateArchiveState.Success(archive)
    }

    private fun extractFirstName(fullName: String?): String =
        fullName?.trim()?.split(" ")?.firstOrNull().orEmpty()
}
