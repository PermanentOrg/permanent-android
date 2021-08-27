package org.permanent.permanent.ui.archives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentArchiveOptionsBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment

const val PARCELABLE_ARCHIVE_ID_KEY = "parcelable_archive_id_key"

class ArchiveOptionsFragment : PermanentBottomSheetFragment(), View.OnClickListener {
    private lateinit var binding: FragmentArchiveOptionsBinding
    private var archiveId: Int? = null
    private val onChangeDefaultArchiveRequest = MutableLiveData<Int>()

    fun setBundleArguments(archiveId: Int) {
        val bundle = Bundle()
        bundle.putInt(PARCELABLE_ARCHIVE_ID_KEY, archiveId)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArchiveOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.btnMakeDefault.setOnClickListener(this)
        archiveId = arguments?.getInt(PARCELABLE_ARCHIVE_ID_KEY)
        return binding.root
    }

    override fun onClick(view: View) {
        dismiss()
        when (view.id) {
            R.id.btnMakeDefault -> {
                archiveId?.let { onChangeDefaultArchiveRequest.value = it }
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

    fun getOnChangeDefaultArchiveRequest(): MutableLiveData<Int> = onChangeDefaultArchiveRequest
}