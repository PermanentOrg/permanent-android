package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.compose.EditMetadataScreen
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.EditMetadataViewModel

class EditMetadataFragment : PermanentBaseFragment()  {
    private lateinit var viewModel: EditMetadataViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[EditMetadataViewModel::class.java]

        arguments?.getParcelableArrayList<Record>(PARCELABLE_FILES_KEY)?.let {
            viewModel.setRecords(it)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EditMetadataScreen()
                }
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
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