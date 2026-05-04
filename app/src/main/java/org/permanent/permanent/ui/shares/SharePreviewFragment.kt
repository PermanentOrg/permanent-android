package org.permanent.permanent.ui.shares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.Constants
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

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: SharePreviewViewModel
    private var urlToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SharePreviewViewModel::class.java]
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(
                org.permanent.permanent.ui.PREFS_NAME, android.content.Context.MODE_PRIVATE
            )
        )
        arguments?.takeIf { it.containsKey(URL_TOKEN_KEY) }?.apply {
            urlToken = getString(URL_TOKEN_KEY)

            if (!urlToken.isNullOrEmpty()) {
                if (prefsHelper.isUserLoggedIn()) {
                    prefsHelper.saveShareLinkUrlToken("")
                    viewModel.checkShareLink(urlToken!!)
                } else {
                    prefsHelper.saveShareLinkUrlToken(urlToken!!)
                    startActivity(Intent(context, AuthenticationActivity::class.java))
                    activity?.finish()
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SharePreviewScreen(
                        viewModel = viewModel,
//                        onChangeArchiveClick = { viewModel.onChangeArchiveBtnClick() },
//                        onViewInArchiveClick = { viewModel.onViewInArchiveBtnClick() },
//                        onOkClick = { viewModel.onOkBtnClick() }
                    )
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

    // VSP-1672: replaced by inline ArchivePickerBottomSheet in SharePreviewScreen. Kept commented for comparison.
//    private val onChangeArchive = Observer<Void?> {
//        urlToken?.let { token ->
//            prefsHelper.saveShareLinkUrlToken(token)
//            archivesContainerFragment = ArchivesContainerFragment()
//            archivesContainerFragment?.show(parentFragmentManager, archivesContainerFragment?.tag)
//            archivesContainerFragment?.getOnCurrentArchiveChanged()?.observe(this, onArchiveChanged)
//        }
//    }
//
//    private val onArchiveChanged = Observer<Void?> {
//        val token = prefsHelper.getShareLinkUrlToken()
//        if (!token.isNullOrEmpty()) {
//            prefsHelper.saveShareLinkUrlToken("")
//            viewModel.checkShareLink(token)
//        }
//    }

    private val onErrorMessage = Observer<String> { message ->
        if (!message.isNullOrEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private val onViewInArchive = Observer<Int?> { recordId ->
        val bundle = bundleOf(
            CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY to Constants.POSITION_SHARED_WITH_ME_FRAGMENT,
            RECORD_ID_TO_NAVIGATE_TO_KEY to recordId
        )
        findNavController().navigate(R.id.action_sharePreviewFragment_to_sharesFragment, bundle)
    }

    private val onNavigateUp = Observer<Void?> {
        findNavController().navigateUp()
    }

    override fun connectViewModelEvents() {
        viewModel.getRecordDisplayName().observe(this, onRecordDisplayName)
        viewModel.getOnViewInArchive().observe(this, onViewInArchive)
        viewModel.getOnNavigateUp().observe(this, onNavigateUp)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getRecordDisplayName().removeObserver(onRecordDisplayName)
        viewModel.getOnViewInArchive().removeObserver(onViewInArchive)
        viewModel.getOnNavigateUp().removeObserver(onNavigateUp)
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
}