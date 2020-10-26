package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyFilesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MyFilesViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.initRecyclerView(binding.rvFiles)
        viewModel.set(parentFragmentManager)

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}