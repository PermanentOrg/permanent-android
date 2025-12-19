package org.permanent.permanent.ui.shareManagement

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.shareManagement.shareLink.ShareLinkScreen
import org.permanent.permanent.viewmodels.ShareManagementViewModel

class ShareLinkFragment : PermanentBottomSheetFragment()  {

    private var record: Record? = null

    private lateinit var viewModel: ShareManagementViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ShareManagementViewModel::class.java]
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        record?.let {
            viewModel.setRecord(it)
        }
        viewModel.setShareLink(arguments?.getParcelable(SHARE_BY_URL_VO_KEY))

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        ) {
                        ShareLinkScreen(
                            viewModel = viewModel,
                            onClose = { dismiss() }
                        )
                    }
                }
            }
        }
    }

    fun setBundleArguments(record: Record?, shareByUrlVO: Shareby_urlVO?) {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record, SHARE_BY_URL_VO_KEY to shareByUrlVO)
        this.arguments = bundle
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
        // Optional: transparent background so your Compose Surface can draw rounded corners
        (requireView().parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun connectViewModelEvents() {}

    override fun disconnectViewModelEvents() {}

    companion object {
        const val PARCELABLE_SHARE_KEY = "parcelable_share_key"
        const val SHARE_BY_URL_VO_KEY = "share_by_url_vo_key"
    }
}