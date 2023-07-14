package org.permanent.permanent.ui.archives

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentContainerArchivesBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.viewmodels.ArchivesContainerViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent


class ArchivesContainerFragment : PermanentBottomSheetFragment() {

    private lateinit var binding: FragmentContainerArchivesBinding
    private lateinit var viewModel: ArchivesContainerViewModel
    private var archivesFragment: ArchivesFragment? = null
    private val onArchiveChanged = SingleLiveEvent<Void?>()

    private val onCurrentArchiveChanged = Observer<Void?> {
        onArchiveChanged.call()
        dismiss()
    }

    private val onDismissObserver = Observer<Void?> {
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ArchivesContainerViewModel::class.java]
        binding = FragmentContainerArchivesBinding.inflate(inflater, container, false)
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
        archivesFragment = ArchivesFragment()
        archivesFragment?.getOnCurrentArchiveChanged()?.observe(this, onCurrentArchiveChanged)
        archivesFragment?.arguments = bundleOf(SHOW_SCREEN_SIMPLIFIED_KEY to true)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, archivesFragment!!).commit()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnDismissRequest().observe(this, onDismissObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnDismissRequest().removeObserver(onDismissObserver)
        archivesFragment?.getOnCurrentArchiveChanged()?.removeObserver(onCurrentArchiveChanged)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnCurrentArchiveChanged(): SingleLiveEvent<Void?> = onArchiveChanged
}