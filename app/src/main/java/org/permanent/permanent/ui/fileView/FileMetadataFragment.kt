package org.permanent.permanent.ui.fileView

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileMetadataBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileMetadataViewModel

class FileMetadataFragment: PermanentBaseFragment() {

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
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = FileMetadataViewPagerAdapter(this)
        fileData?.let { viewAdapter.setFileData(it) }
        val viewPager2 = binding.vpFileDetails
        viewPager2.adapter = viewAdapter
        viewPager2.isSaveEnabled = false

        TabLayoutMediator(binding.tlFileDetails, viewPager2) { tab, position ->
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

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    private fun updateActionBarAndStatusBar(color: Int) {
        val window: Window? = activity?.window
        val supportActionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (color == Color.BLACK) {
            supportActionBar?.title = fileData?.displayName
            supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            window?.statusBarColor = Color.BLACK
        } else {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
            window?.statusBarColor = color
        }
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        updateActionBarAndStatusBar(Color.BLACK)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        updateActionBarAndStatusBar(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
    }
}