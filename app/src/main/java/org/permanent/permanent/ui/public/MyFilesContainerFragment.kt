package org.permanent.permanent.ui.public

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentContainerBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.MyFilesFragment
import org.permanent.permanent.ui.myFiles.SHOWN_IN_WHICH_WORKSPACE
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.viewmodels.MyFilesContainerViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class MyFilesContainerFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentContainerBinding
    private lateinit var viewModel: MyFilesContainerViewModel
    private var shownInWorkspace: Workspace? = Workspace.PUBLIC_ARCHIVES
    private var myFilesFragment: MyFilesFragment? = null
    private val onRecordSelectedEvent = SingleLiveEvent<Record>()
    private val onSaveFolderEvent = SingleLiveEvent<Record>()

    private val onSaveFolderRequestObserver = Observer<Void> {
        val record = onRecordSelectedEvent.value
        val destinationFolder = if (record?.type == RecordType.FOLDER) record else null
        onSaveFolderEvent.value = destinationFolder
        dismiss()
    }

    private val onCancelRequestObserver = Observer<Void> {
        dismiss()
    }

    private val onRecordSelectedObserver = Observer<Record> {
        onRecordSelectedEvent.value = it
        if (shownInWorkspace == Workspace.PUBLIC_ARCHIVES && it.type == RecordType.FILE) dismiss()
    }

    fun setBundleArguments(workspace: Workspace) {
        val bundle = Bundle()
        bundle.putParcelable(SHOWN_IN_WHICH_WORKSPACE, workspace)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MyFilesContainerViewModel::class.java]
        binding = FragmentContainerBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        shownInWorkspace = arguments?.getParcelable(SHOWN_IN_WHICH_WORKSPACE)
        viewModel.setShownInWorkspace(shownInWorkspace)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        myFilesFragment = MyFilesFragment()
        myFilesFragment?.getOnRecordSelected()?.observe(this, onRecordSelectedObserver)
        myFilesFragment?.arguments = bundleOf(SHOW_SCREEN_SIMPLIFIED_KEY to true)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, myFilesFragment!!).commit()
    }

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelectedEvent

    fun getOnSaveFolderEvent(): MutableLiveData<Record> = onSaveFolderEvent

    override fun connectViewModelEvents() {
        viewModel.getOnSaveFolderRequest().observe(this, onSaveFolderRequestObserver)
        viewModel.getOnCancelRequest().observe(this, onCancelRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSaveFolderRequest().removeObserver(onSaveFolderRequestObserver)
        viewModel.getOnCancelRequest().removeObserver(onCancelRequestObserver)
        myFilesFragment?.getOnRecordSelected()?.removeObserver(onRecordSelectedObserver)
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