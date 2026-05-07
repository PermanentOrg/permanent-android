package org.permanent.permanent.ui.shares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.AuthenticationActivity
import org.permanent.permanent.ui.shares.compose.SharePreviewScreen
import org.permanent.permanent.viewmodels.SharePreviewViewModel


const val URL_TOKEN_KEY = "url_token"
const val RECORD_ID_TO_NAVIGATE_TO_KEY = "record_id_to_navigate_to"
const val CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY = "child_fragment_to_navigate_to"
const val SHOW_SCREEN_SIMPLIFIED_KEY = "show_screen_simplified"

class SharePreviewFragment : PermanentBaseFragment() {

    private lateinit var viewModel: SharePreviewViewModel

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.restoreOriginalArchiveIfChanged {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SharePreviewViewModel::class.java]
        val prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(
                org.permanent.permanent.ui.PREFS_NAME, android.content.Context.MODE_PRIVATE
            )
        )
        val urlToken = arguments?.getString(URL_TOKEN_KEY)
        if (!urlToken.isNullOrEmpty()) {
            if (prefsHelper.isUserLoggedIn()) {
                prefsHelper.saveShareLinkUrlToken("")
                viewModel.checkShareLink(urlToken)
            } else {
                prefsHelper.saveShareLinkUrlToken(urlToken)
                startActivity(Intent(context, AuthenticationActivity::class.java))
                activity?.finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SharePreviewScreen(viewModel = viewModel)
                }
            }
        }
    }

    private val onRecordDisplayName = Observer<String> { title ->
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)

        lifecycleScope.launchWhenStarted {
            if (!title.isNullOrEmpty()) {
                toolbar.title = title
            }
        }
    }

    private val onErrorMessage = Observer<String> { message ->
        if (!message.isNullOrEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private val onSharePreviewNavEvent = Observer<SharePreviewNavEvent> { event ->
        val bundle = bundleOf(
            CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY to event.tabPosition,
            RECORD_ID_TO_NAVIGATE_TO_KEY to (event.itemId ?: 0)
        )
        findNavController().navigate(R.id.action_sharePreviewFragment_to_sharesFragment, bundle)
    }

    override fun connectViewModelEvents() {
        viewModel.getRecordDisplayName().observe(this, onRecordDisplayName)
        viewModel.getOnSharePreviewNavEvent().observe(this, onSharePreviewNavEvent)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getRecordDisplayName().removeObserver(onRecordDisplayName)
        viewModel.getOnSharePreviewNavEvent().removeObserver(onSharePreviewNavEvent)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fallback for non-back exits (e.g. drawer item tapped). Best-effort: writes prefs
        // synchronously, fires the archive switch, and doesn't wait — the new destination is
        // expected to load its data lazily and tolerate a brief session lag.
        viewModel.restoreOriginalArchiveIfChanged {}
    }
}
