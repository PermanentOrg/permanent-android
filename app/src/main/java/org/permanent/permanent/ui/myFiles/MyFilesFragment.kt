package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.Constants
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment :
    PermanentBaseFragment(),
    PermanentTextWatcher,
    View.OnClickListener {

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
        binding.etSearchQuery.addTextChangedListener(this)
        binding.clFolderDropdown.setOnClickListener(this)

        return binding.root
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        viewAdapter.filter.filter(charSequence)
        binding.ivSearchIcon.visibility =
            if (charSequence.toString().isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onClick(view: View) {
        val bottomDrawerFragment = FolderOptionsFragment()
        val bundle = Bundle()
        bundle.putString(Constants.FOLDER_NAME, Constants.MY_FILES_FOLDER)
        bottomDrawerFragment.arguments = bundle
        bottomDrawerFragment.show((context as AppCompatActivity).supportFragmentManager, bottomDrawerFragment.tag)
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}