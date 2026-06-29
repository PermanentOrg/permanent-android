package org.permanent.permanent.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.viewmodels.DashboardViewModel

/**
 * Top-level widget Dashboard, shown to users who have no archive yet. Lives in the main
 * navigation graph (see MainActivity for the start-destination routing).
 */
class DashboardFragment : PermanentBaseFragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private val prefsHelper by lazy {
        PreferencesHelper(requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    DashboardScreen(
                        viewModel = viewModel,
                        onGoToArchive = {
                            // Land on Private Files: select that workspace and clear any stale
                            // "show archives" deep-link flag (e.g. left over from an earlier
                            // Archives → sign-in bounce) that would otherwise make MyFilesFragment
                            // redirect to the Archives screen.
                            prefsHelper.saveCurrentWorkspace(Workspace.PRIVATE_FILES)
                            prefsHelper.saveShowArchivesDeepLink(false)
                            findNavController().navigate(
                                R.id.action_dashboardFragment_to_myFilesFragment
                            )
                        }
                    )
                }
            }
        }
    }

    override fun connectViewModelEvents() {}

    override fun disconnectViewModelEvents() {}

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}
