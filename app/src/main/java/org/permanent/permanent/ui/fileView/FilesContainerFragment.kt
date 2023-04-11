package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFilesContainerBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.FilesContainerViewModel

class FilesContainerFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentFilesContainerBinding
    private lateinit var viewAdapter: FilesContainerPagerAdapter
    private lateinit var viewModel: FilesContainerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FilesContainerViewModel::class.java)
        binding = FragmentFilesContainerBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setHasOptionsMenu(true)
        // Requesting permission for viewing files stored locally
        val permissionHelper = DevicePermissionsHelper()
        if (!permissionHelper.hasReadStoragePermission(requireContext())) {
            permissionHelper.requestReadStoragePermission(this)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_file_view, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getParcelableArrayList<Record>(PARCELABLE_FILES_KEY)?.let {
            viewAdapter = FilesContainerPagerAdapter(this, it)
            val viewPager = binding.vpFileView
            viewPager.adapter = viewAdapter
            viewPager.currentItem = getFileIndexToStartWith(it)
            viewPager.offscreenPageLimit = 2 //2 before and 2 after the currentItem
        }
    }

    private fun getFileIndexToStartWith(files: ArrayList<Record>): Int {
        for ((index, file) in files.withIndex())
            if (file.displayFirstInCarousel) return index
        return 0
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