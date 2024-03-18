package org.permanent.permanent.ui.bulkEditMetadata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.bulkEditMetadata.compose.EditFileNamesScreen
import org.permanent.permanent.ui.bulkEditMetadata.compose.EditMetadataScreen

class EditFileNamesFragment : PermanentBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EditFileNamesScreen()
                }
            }
        }
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }

}