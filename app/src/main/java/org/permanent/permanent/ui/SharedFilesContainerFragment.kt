package org.permanent.permanent.ui

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
import org.permanent.permanent.databinding.FragmentContainerSharedFilesBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.ui.shares.SharesFragment
import org.permanent.permanent.viewmodels.SharedFilesContainerViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class SharedFilesContainerFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentContainerSharedFilesBinding
    private lateinit var viewModel: SharedFilesContainerViewModel
    private var sharesFragment: SharesFragment? = null
    private lateinit var selectedDestinationRecord: Pair<Workspace, Record>
    private val onSaveFolderEvent = SingleLiveEvent<Pair<Workspace, Record?>>()

    private val onSaveFolderRequestObserver = Observer<Void> {
        onSaveFolderEvent.value = selectedDestinationRecord
        dismiss()
    }

    private val onCancelRequestObserver = Observer<Void> {
        dismiss()
    }

    private val onRecordSelectedObserver = Observer<Pair<Workspace, Record>> {
        selectedDestinationRecord = it
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SharedFilesContainerViewModel::class.java]
        binding = FragmentContainerSharedFilesBinding.inflate(inflater, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharesFragment = SharesFragment()
        sharesFragment?.getOnRecordSelected()?.observe(this, onRecordSelectedObserver)
        sharesFragment?.arguments = bundleOf(SHOW_SCREEN_SIMPLIFIED_KEY to true)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, sharesFragment!!).commit()
    }

    fun getOnSaveFolderEvent(): MutableLiveData<Pair<Workspace, Record?>> = onSaveFolderEvent

    override fun connectViewModelEvents() {
        viewModel.getOnSaveFolderRequest().observe(this, onSaveFolderRequestObserver)
        viewModel.getOnCancelRequest().observe(this, onCancelRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSaveFolderRequest().removeObserver(onSaveFolderRequestObserver)
        viewModel.getOnCancelRequest().removeObserver(onCancelRequestObserver)
        sharesFragment?.getOnRecordSelected()?.removeObserver(onRecordSelectedObserver)
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