package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.databinding.FragmentSortOptionsBinding
import org.permanent.permanent.viewmodels.SortOptionsViewModel

class SortOptionsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSortOptionsBinding
    private lateinit var viewModel: SortOptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSortOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(SortOptionsViewModel::class.java)
        binding.viewModel = viewModel
        binding.tvFolderName.text = arguments?.getString(Constants.FOLDER_NAME)

        return binding.root
    }
}