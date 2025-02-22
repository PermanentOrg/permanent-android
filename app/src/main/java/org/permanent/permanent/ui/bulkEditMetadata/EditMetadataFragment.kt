package org.permanent.permanent.ui.bulkEditMetadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.bulkEditMetadata.compose.EditMetadataScreen
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.EditMetadataViewModel

class EditMetadataFragment : PermanentBaseFragment() {

    private lateinit var viewModel: EditMetadataViewModel
    private var newTagFragment: NewTagFragment? = null
    private var locationFragment: EditLocationFragment? = null
    private var dateFragment: EditDateTimeFragment? = null
    private var fileNameFragment: EditFileNamesFragment? = null
    private var records = ArrayList<Record>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[EditMetadataViewModel::class.java]

        arguments?.getParcelableArrayList<Record>(PARCELABLE_FILES_KEY)?.let {
            viewModel.setRecords(it)
            records.addAll(it)
        }

        val lifecycleOwner = viewLifecycleOwner

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EditMetadataScreen(
                        viewModel = viewModel,
                        openNewTagScreen = {
                            newTagFragment = NewTagFragment()
                            newTagFragment?.show(parentFragmentManager, newTagFragment?.tag)
                            newTagFragment?.setBundleArguments(records, it)
                            newTagFragment?.getOnTagsAddedToSelection()
                                ?.observe(lifecycleOwner, onTagsAddedToSelectionObserver)
                        },
                        openEditFileNamesScreen = {
                            fileNameFragment = EditFileNamesFragment()
                            fileNameFragment?.setBundleArguments(records)
                            fileNameFragment?.show(parentFragmentManager, fileNameFragment?.tag)
                            fileNameFragment?.getOnFilenameChanged()
                                ?.observe(lifecycleOwner, onFilenameChangedObserver)
                        },
                        openDateAndTimeScreen = {
                            dateFragment = EditDateTimeFragment()
                            dateFragment?.setBundleArguments(records)
                            dateFragment?.show(parentFragmentManager, dateFragment?.tag)
                            dateFragment?.getOnDateChanged()
                                ?.observe(lifecycleOwner, onDateChangedObserver)
                        },
                        openLocationScreen = {
                            locationFragment = EditLocationFragment()
                            locationFragment?.setBundleArguments(records)
                            locationFragment?.show(parentFragmentManager, locationFragment?.tag)
                            locationFragment?.getOnLocationChanged()
                                ?.observe(lifecycleOwner, onLocationChangedObserver)
                        }
                    )
                }
            }
        }
    }

    private val onTagsAddedToSelectionObserver = Observer<List<Tag>> {
        viewModel.onTagsAddedToSelection(it)
    }

    private val onLocationChangedObserver = Observer<String> {
        viewModel.onLocationChanged(it)
    }

    private val onDateChangedObserver = Observer<String> {
        viewModel.onDateChanged(it)
    }

    private val onFilenameChangedObserver = Observer<String> {
        viewModel.showInfoMessage.value = it
    }


    override fun connectViewModelEvents() {

    }

    override fun disconnectViewModelEvents() {
        newTagFragment?.getOnTagsAddedToSelection()?.removeObserver(onTagsAddedToSelectionObserver)
        locationFragment?.getOnLocationChanged()?.removeObserver(onLocationChangedObserver)
        dateFragment?.getOnDateChanged()?.removeObserver(onDateChangedObserver)
        fileNameFragment?.getOnFilenameChanged()?.removeObserver(onFilenameChangedObserver)
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