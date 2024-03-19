package org.permanent.permanent.ui.bulkEditMetadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.bulkEditMetadata.compose.EditFileNamesScreen
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.EditFileNamesViewModel

class EditFileNamesFragment : PermanentBottomSheetFragment() {

    private lateinit var viewModel: EditFileNamesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[EditFileNamesViewModel::class.java]

        arguments?.getParcelableArrayList<Record>(PARCELABLE_FILES_KEY)?.let {
            viewModel.setRecords(it)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EditFileNamesScreen(viewModel)
                }
            }
        }
    }

    fun setBundleArguments(records: ArrayList<Record>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(PARCELABLE_FILES_KEY, records)
        this.arguments = bundle
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }

}