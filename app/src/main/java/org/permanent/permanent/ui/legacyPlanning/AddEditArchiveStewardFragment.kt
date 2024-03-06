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
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.legacyPlanning.compose.AddEditLegacyContactScreen
import org.permanent.permanent.viewmodels.AddEditArchiveStewardViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class AddEditArchiveStewardFragment : PermanentBottomSheetFragment() {

    private var archiveId: Int? = null
    private var archiveSteward: ArchiveSteward? = null
    private lateinit var viewModel: AddEditArchiveStewardViewModel
    private val onArchiveStewardUpdated = SingleLiveEvent<ArchiveSteward>()

    fun setBundleArguments(archiveId: Int?, archiveSteward: ArchiveSteward?) {
        val bundle = Bundle()
        archiveId?.let { bundle.putInt(ARCHIVE_ID_KEY, it) }
        bundle.putParcelable(ARCHIVE_STEWARD_KEY, archiveSteward)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        archiveId = arguments?.getInt(ARCHIVE_ID_KEY)
        archiveSteward = arguments?.getParcelable(ARCHIVE_STEWARD_KEY)

        viewModel = ViewModelProvider(this)[AddEditArchiveStewardViewModel::class.java]
        viewModel.setArchiveId(archiveId)
        viewModel.setArchiveSteward(archiveSteward)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AddEditLegacyContactScreen(
                        viewModel,
                        screenTitle = stringResource(R.string.archive_steward),
                        title = stringResource(R.string.designate_archive_steward),
                        subtitle = stringResource(R.string.designate_archive_steward_description),
                        namePlaceholder = stringResource(R.string.steward_name),
                        emailPlaceholder = stringResource(R.string.steward_email_address),
                        note = stringResource(R.string.steward_note_description),
                        showName = false,
                        showMessage = true
                    ) { this@AddEditArchiveStewardFragment.dismiss() }
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

    private val onArchiveStewardUpdatedObserver = Observer<ArchiveSteward> {
        onArchiveStewardUpdated.value = it
        this.dismiss()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchiveStewardUpdated().observe(this, onArchiveStewardUpdatedObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveStewardUpdated().removeObserver(onArchiveStewardUpdatedObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnArchiveStewardUpdated(): MutableLiveData<ArchiveSteward> = onArchiveStewardUpdated

    companion object {
        const val ARCHIVE_ID_KEY = "archive_id_key"
        const val ARCHIVE_STEWARD_KEY = "archive_steward_key"
    }
}