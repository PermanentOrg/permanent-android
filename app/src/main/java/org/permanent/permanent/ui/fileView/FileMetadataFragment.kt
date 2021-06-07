package org.permanent.permanent.ui.fileView

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileMetadataBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileMetadataViewModel

class FileMetadataFragment: PermanentBaseFragment(), View.OnClickListener {

    private lateinit var viewModel: FileMetadataViewModel
    private lateinit var viewAdapter: FileMetadataViewPagerAdapter
    private lateinit var binding: FragmentFileMetadataBinding
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileMetadataViewModel::class.java)
        binding = FragmentFileMetadataBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
            (activity as AppCompatActivity?)?.supportActionBar?.title = fileData?.displayName
        }
        binding.executePendingBindings()
        binding.ivThumbnail.setOnClickListener(this)
        val shouldScroll = arguments?.getBoolean(BOOLEAN_SHOULD_SCROLL_KEY)
        shouldScroll?.let { if (it) binding.vpFileMetadata.requestFocus() }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_file_view, menu)
        menu.findItem(R.id.metadataItem).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = FileMetadataViewPagerAdapter(this)
        fileData?.let { viewAdapter.setFileData(it) }
        val viewPager2 = binding.vpFileMetadata
        viewPager2.adapter = viewAdapter
        viewPager2.isSaveEnabled = false

        TabLayoutMediator(binding.tlFileMetadata, viewPager2) { tab, position ->
            when (position) {
                Constants.POSITION_DETAILS_FRAGMENT -> tab.text =
                    getString(R.string.metadata_details_tab_name)
                else -> tab.text = getString(R.string.metadata_info_tab_name)
            }
        }.attach()
    }

    private val onShowMessage = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onClick(v: View?) { // On Thumbnail click
        findNavController().popBackStack(R.id.filesContainerFragment, false)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }
}