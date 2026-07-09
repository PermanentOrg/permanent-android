package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFilesContainerBinding
import org.permanent.permanent.models.FileSessionData
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
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
        // Pages of the previous adapter survive view recreation (back from the metadata
        // screen) as orphans in the childFragmentManager — they double-handle menu events
        // and hold stale state. Remove them before the new adapter populates.
        if (childFragmentManager.fragments.isNotEmpty()) {
            val transaction = childFragmentManager.beginTransaction()
            childFragmentManager.fragments.forEach { transaction.remove(it) }
            transaction.commitNowAllowingStateLoss()
        }
        val records = FileSessionData.records
        records?.let {
            viewAdapter = FilesContainerPagerAdapter(this, it)
            val viewPager = binding.vpFileView
            // A fresh adapter is created on every view recreation (e.g. back from the
            // metadata screen), but the pager's framework state restore re-applies the
            // OLD adapter's fragment state onto it and crashes destroyItem with an
            // out-of-bounds index. The position is tracked manually instead.
            viewPager.isSaveEnabled = false
            viewPager.adapter = viewAdapter
            viewPager.currentItem =
                if (viewModel.currentFilePosition in it.indices) viewModel.currentFilePosition
                else getFileIndexToStartWith(it)
            viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    viewModel.currentFilePosition = position
                }
            })
            viewPager.offscreenPageLimit = 2 //2 before and 2 after the currentItem
        }
    }

    private fun getFileIndexToStartWith(files: List<Record>): Int {
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