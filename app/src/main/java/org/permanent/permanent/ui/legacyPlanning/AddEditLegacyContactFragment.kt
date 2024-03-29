package org.permanent.permanent.ui.legacyPlanning

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.R
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.legacyPlanning.compose.AddEditLegacyContactScreen
import org.permanent.permanent.viewmodels.AddEditLegacyContactViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class AddEditLegacyContactFragment : PermanentBottomSheetFragment() {

    private var legacyContact: LegacyContact? = null
    private lateinit var viewModel: AddEditLegacyContactViewModel
    private val onLegacyContactUpdated = SingleLiveEvent<LegacyContact>()

    fun setBundleArguments(legacyContact: LegacyContact?) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_LEGACY_CONTACT_KEY, legacyContact)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        legacyContact = arguments?.getParcelable(PARCELABLE_LEGACY_CONTACT_KEY)

        viewModel = ViewModelProvider(this)[AddEditLegacyContactViewModel::class.java]
        viewModel.setContact(legacyContact)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AddEditLegacyContactScreen(
                        viewModel = viewModel,
                        screenTitle = stringResource(R.string.legacy_contact),
                        title = stringResource(R.string.designate_account_legacy_contact),
                        subtitle = stringResource(R.string.designate_account_legacy_contact_description),
                        namePlaceholder = stringResource(R.string.contact_name),
                        emailPlaceholder = stringResource(R.string.contact_email_address),
                        note = stringResource(R.string.note_description),
                        showName = true,
                        showMessage = false
                    ) { this@AddEditLegacyContactFragment.dismiss() }
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

    private val onLegacyContactUpdatedObserver = Observer<LegacyContact> {
        onLegacyContactUpdated.value = it
        this.dismiss()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLegacyContactUpdated().observe(this, onLegacyContactUpdatedObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLegacyContactUpdated().removeObserver(onLegacyContactUpdatedObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnLegacyContactUpdated(): MutableLiveData<LegacyContact> = onLegacyContactUpdated

    companion object {
        const val PARCELABLE_LEGACY_CONTACT_KEY = "parcelable_legacy_contact_key"
    }
}