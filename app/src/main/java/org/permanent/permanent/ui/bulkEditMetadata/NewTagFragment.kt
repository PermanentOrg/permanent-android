package org.permanent.permanent.ui.bulkEditMetadata

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.compose.bulkEditMetadata.NewTagScreen
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.NewTagViewModel

class NewTagFragment : PermanentBottomSheetFragment() {

    private lateinit var viewModel: NewTagViewModel
    private val onTagsAddedToSelection = MutableLiveData<List<Tag>>()

    fun setBundleArguments(records: ArrayList<Record>, recentTags: ArrayList<Tag>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(PARCELABLE_FILES_KEY, records)
        bundle.putParcelableArrayList(RECENT_TAGS_KEY, recentTags)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[NewTagViewModel::class.java]
        viewModel.setRecords(arguments?.getParcelableArrayList(PARCELABLE_FILES_KEY))
        viewModel.setRecentTags(arguments?.getParcelableArrayList(RECENT_TAGS_KEY))

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    NewTagScreen(viewModel) { this@NewTagFragment.dismiss() }
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

    private val onTagsAddedToSelectionObserver = Observer<List<Tag>> {
        onTagsAddedToSelection.value = it
        this.dismiss()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnTagsAddedToSelection().observe(this, onTagsAddedToSelectionObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnTagsAddedToSelection().removeObserver(onTagsAddedToSelectionObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnTagsAddedToSelection() = onTagsAddedToSelection

    companion object {
        const val RECENT_TAGS_KEY = "recent_tags_key"
    }
}