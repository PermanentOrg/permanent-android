package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.databinding.FileOptionsBinding
import org.permanent.permanent.Constants
import org.permanent.permanent.viewmodels.FileOptionsViewModel

class FileOptionsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FileOptionsBinding
    private lateinit var viewModel: FileOptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FileOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(FileOptionsViewModel::class.java)
        binding.viewModel = viewModel
        binding.tvFileName.text = arguments?.getString(Constants.FILE_NAME)

        return binding.root
    }
}