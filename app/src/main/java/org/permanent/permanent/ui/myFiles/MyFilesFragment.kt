package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment(), PermanentTextWatcher {

    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private lateinit var viewAdapter: FilesAdapter

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
        binding.etSearchQuery.addTextChangedListener(this)

        return binding.root
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        viewAdapter.filter.filter(charSequence)
        binding.ivSearchIcon.visibility =
            if (charSequence.toString().isEmpty()) View.VISIBLE else View.GONE
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}