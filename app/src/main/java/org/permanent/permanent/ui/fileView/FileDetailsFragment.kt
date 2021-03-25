package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentFileDetailsBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileDetailsViewModel

class FileDetailsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: FileDetailsViewModel
    private lateinit var binding: FragmentFileDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileDetailsViewModel::class.java)
        binding = FragmentFileDetailsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        arguments?.takeIf { it.containsKey(PARCELABLE_FILE_DATA_KEY) }?.apply {
            getParcelable<FileData>(PARCELABLE_FILE_DATA_KEY)?.also { viewModel.setFileData(it) }
        }
        return binding.root
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