package org.permanent.permanent.ui.settings

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.EventsManager
import org.permanent.permanent.R
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.settings.compose.SettingsMenuScreen
import org.permanent.permanent.viewmodels.SettingsMenuViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class SettingsMenuFragment : PermanentBottomSheetFragment() {

    private lateinit var viewModel: SettingsMenuViewModel
    private val onLegacyContactUpdated = SingleLiveEvent<LegacyContact>()

    fun setBundleArguments(legacyContact: LegacyContact?) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_LEGACY_CONTACT_KEY, legacyContact)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SettingsMenuViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SettingsMenuScreen(
                        viewModel = viewModel,
                        onCloseScreenClick = { this@SettingsMenuFragment.dismiss() },
                        onAccountClick = {
                            findNavController().navigate(R.id.accountFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onStorageClick = {
                            findNavController().navigate(R.id.storageMenuFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onMyArchivesClick = {
                            findNavController().navigate(R.id.archivesFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onInvitationsClick = {
                            findNavController().navigate(R.id.invitationsFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onActivityFeedClick = {
                            findNavController().navigate(R.id.activityFeedFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onSecurityClick = {
                            findNavController().navigate(R.id.securityFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onLegacyPlanningClick = {
                            findNavController().navigate(R.id.legacyLoadingFragment)
                            this@SettingsMenuFragment.dismiss()
                        },
                        onSignOutClick = {
                            viewModel.deleteDeviceToken()
                            EventsManager(context).resetUser()
                        })
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val sheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                sheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private val onLoggedOut = Observer<Void?> {
        context?.let { ctx -> EventsManager(ctx).resetUser() }
        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedOut().observe(this, onLoggedOut)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedOut().removeObserver(onLoggedOut)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateArchiveAndAccountDetails()
        viewModel.updateUsedStorage()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val PARCELABLE_LEGACY_CONTACT_KEY = "parcelable_legacy_contact_key"
    }
}