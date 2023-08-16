package org.permanent.permanent.ui.archives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentArchiveOptionsBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.ArchiveOptionsViewModel

const val PARCELABLE_ARCHIVE_KEY = "parcelable_archive_key"

class ArchiveOptionsFragment : PermanentBottomSheetFragment(), View.OnClickListener {
    private lateinit var binding: FragmentArchiveOptionsBinding
    private lateinit var viewModel: ArchiveOptionsViewModel
    private var archive: Archive? = null
    private val onChangeDefaultArchiveRequest = MutableLiveData<Int>()
    private val onDeleteArchiveRequest = MutableLiveData<Archive>()
    private val onConfigureStewardRequest = MutableLiveData<Archive>()

    fun setBundleArguments(archive: Archive) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_ARCHIVE_KEY, archive)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ArchiveOptionsViewModel::class.java)
        binding = FragmentArchiveOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.btnMakeDefault.setOnClickListener(this)
        binding.btnDeleteArchive.setOnClickListener(this)
        binding.btnConfigureSteward.setOnClickListener(this)
        archive = arguments?.getParcelable(PARCELABLE_ARCHIVE_KEY)
        viewModel.setArchive(archive)
        return binding.root
    }

    override fun onClick(view: View) {
        dismiss()
        when (view.id) {
            R.id.btnMakeDefault -> {
                onChangeDefaultArchiveRequest.value = archive?.id
            }
            R.id.btnDeleteArchive -> {
                onDeleteArchiveRequest.value = archive
            }
            R.id.btnConfigureSteward -> {
                onConfigureStewardRequest.value = archive
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

    fun getOnConfigureStewardRequest(): MutableLiveData<Archive> = onConfigureStewardRequest

    fun getOnDeleteArchiveRequest(): MutableLiveData<Archive> = onDeleteArchiveRequest
}