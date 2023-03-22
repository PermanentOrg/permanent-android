package org.permanent.permanent.ui.myFiles.saveToPermanent

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_title_text_two_buttons.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentChooseFolderBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.PublicFilesContainerFragment
import org.permanent.permanent.ui.SharedFilesContainerFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.public.MyFilesContainerFragment
import org.permanent.permanent.viewmodels.ChooseFolderViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class ChooseFolderFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentChooseFolderBinding
    private lateinit var viewModel: ChooseFolderViewModel
    private var onDestinationFolderChangedEvent = SingleLiveEvent<Pair<Workspace, Record?>>()
    private var privateFilesContainerFragment: MyFilesContainerFragment? = null
    private var publicFilesContainerFragment: PublicFilesContainerFragment? = null
    private var sharedFilesContainerFragment: SharedFilesContainerFragment? = null
    private var workspace = Workspace.PRIVATE_FILES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ChooseFolderViewModel::class.java]
        binding = FragmentChooseFolderBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val dialogc = dialog as BottomSheetDialog
            val bottomSheet =
                dialogc.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private val onPrivateFilesSelectedObserver = Observer<Void> {
        workspace = Workspace.PRIVATE_FILES
        privateFilesContainerFragment = MyFilesContainerFragment()
        privateFilesContainerFragment?.setBundleArguments(Workspace.PRIVATE_FILES)
        privateFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
        privateFilesContainerFragment?.show(parentFragmentManager, privateFilesContainerFragment?.tag)
    }

    private val onSharedFilesSelectedObserver = Observer<Void> {
        workspace = Workspace.SHARES
        sharedFilesContainerFragment = SharedFilesContainerFragment()
        sharedFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
        sharedFilesContainerFragment?.show(parentFragmentManager, sharedFilesContainerFragment?.tag)
    }

    private val onPublicFilesSelectedObserver = Observer<Void> {
        showConfirmationDialog()
    }

    private fun showConfirmationDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_title_text_two_buttons, null)
        val alert = android.app.AlertDialog.Builder(context).setView(viewDialog).create()

        viewDialog.tvTitle.text = getString(R.string.menu_drawer_public_files)
        viewDialog.tvText.text = getString(R.string.save_to_permanent_upload_to_public_files)
        viewDialog.btnPositive.text = getString(R.string.button_continue)
        viewDialog.btnPositive.setOnClickListener {
            showPublicFilesContainer()
            alert.dismiss()
        }
        viewDialog.btnNegative.text = getString(R.string.button_cancel)
        viewDialog.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private fun showPublicFilesContainer() {
        workspace = Workspace.PUBLIC_FILES
        publicFilesContainerFragment = PublicFilesContainerFragment()
        publicFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
        publicFilesContainerFragment?.show(parentFragmentManager, publicFilesContainerFragment?.tag)
    }

    private val onCancelRequestObserver = Observer<Void> {
        dismiss()
    }

    private val onFolderChangedObserver = Observer<Pair<Workspace, Record?>> {
        onDestinationFolderChangedEvent.value = it
        dismiss()
    }

    fun getOnFolderChangedEvent(): MutableLiveData<Pair<Workspace, Record?>> =
        onDestinationFolderChangedEvent

    override fun connectViewModelEvents() {
        viewModel.getOnCancelRequest().observe(this, onCancelRequestObserver)
        viewModel.getOnPrivateFilesSelected().observe(this, onPrivateFilesSelectedObserver)
        viewModel.getOnSharedFilesSelected().observe(this, onSharedFilesSelectedObserver)
        viewModel.getOnPublicFilesSelected().observe(this, onPublicFilesSelectedObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnCancelRequest().removeObserver(onCancelRequestObserver)
        viewModel.getOnPrivateFilesSelected().removeObserver(onPrivateFilesSelectedObserver)
        viewModel.getOnSharedFilesSelected().removeObserver(onSharedFilesSelectedObserver)
        viewModel.getOnPublicFilesSelected().removeObserver(onPublicFilesSelectedObserver)
        privateFilesContainerFragment?.getOnSaveFolderEvent()
            ?.removeObserver(onFolderChangedObserver)
        sharedFilesContainerFragment?.getOnSaveFolderEvent()
            ?.removeObserver(onFolderChangedObserver)
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