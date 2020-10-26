package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.databinding.FragmentFolderOptionsBinding
import org.permanent.permanent.Constants
import org.permanent.permanent.viewmodels.FolderOptionsViewModel

class FolderOptionsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFolderOptionsBinding
    private lateinit var viewModel: FolderOptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFolderOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(FolderOptionsViewModel::class.java)
        binding.viewModel = viewModel
        binding.tvFolderName.text = arguments?.getString(Constants.FOLDER_NAME)

        return binding.root
    }
}