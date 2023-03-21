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
import org.permanent.permanent.databinding.FragmentChooseFolderBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.SharedFilesContainerFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.public.MyFilesContainerFragment
import org.permanent.permanent.viewmodels.ChooseFolderViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class ChooseFolderFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentChooseFolderBinding
    private lateinit var viewModel: ChooseFolderViewModel
    private var onDestinationFolderChangedEvent = SingleLiveEvent<Pair<Workspace, Record?>>()
    private var myFilesContainerFragment: MyFilesContainerFragment? = null
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
        myFilesContainerFragment = MyFilesContainerFragment()
        myFilesContainerFragment?.setBundleArguments(Workspace.PRIVATE_FILES)
        myFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
        myFilesContainerFragment?.show(parentFragmentManager, myFilesContainerFragment?.tag)
    }

    private val onSharedFilesSelectedObserver = Observer<Void> {
        workspace = Workspace.SHARES
        sharedFilesContainerFragment = SharedFilesContainerFragment()
        sharedFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
        sharedFilesContainerFragment?.show(parentFragmentManager, myFilesContainerFragment?.tag)
    }

    private val onPublicFilesSelectedObserver = Observer<Void> {
        workspace = Workspace.PUBLIC_FILES
//        myFilesContainerFragment = MyFilesContainerFragment()
//        myFilesContainerFragment?.setBundleArguments(Workspace.PRIVATE_FILES)
//        myFilesContainerFragment?.getOnSaveFolderEvent()?.observe(this, onFolderChangedObserver)
//        myFilesContainerFragment?.show(parentFragmentManager, myFilesContainerFragment?.tag)
    }

    private val onCancelRequestObserver = Observer<Void> {
        dismiss()
    }

    private val onFolderChangedObserver = Observer<Pair<Workspace, Record?>> {
        onDestinationFolderChangedEvent.value = it
        dismiss()
    }

    fun getOnFolderChangedEvent(): MutableLiveData<Pair<Workspace, Record?>> = onDestinationFolderChangedEvent

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
        myFilesContainerFragment?.getOnSaveFolderEvent()?.removeObserver(onFolderChangedObserver)
        sharedFilesContainerFragment?.getOnSaveFolderEvent()?.removeObserver(onFolderChangedObserver)
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