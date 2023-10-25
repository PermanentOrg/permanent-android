package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentAddEditFileTagsBinding
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.AddEditFileTagsViewModel

class AddEditFileTagsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: AddEditFileTagsViewModel
    private lateinit var binding: FragmentAddEditFileTagsBinding
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddEditFileTagsViewModel::class.java]
        binding = FragmentAddEditFileTagsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
        }
        setHasOptionsMenu(true)
        initDeviceBackPressCallback()
        return binding.root
    }

    private fun initDeviceBackPressCallback() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp(fileData)
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_done_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.doneItem -> {
                viewModel.updateTagsOnServer()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val onTagsFiltered = Observer<List<Tag>> { archiveTags ->
        val chipGroup = binding.chipGroupAllTags
        chipGroup.removeAllViews()
        for (archiveTag in archiveTags) {
            val chip = layoutInflater.inflate(
                R.layout.item_chip_filter_white, chipGroup, false) as Chip
            chip.text = (archiveTag.name)
            chip.isChecked = archiveTag.isCheckedOnLocal
            chip.setEnsureMinTouchTargetSize(false)
            chip.setOnCheckedChangeListener { _, isChecked ->
                archiveTag.isCheckedOnLocal = isChecked
            }
            chipGroup.addView(chip)
        }
    }

    private val onTagsUpdated = Observer<FileData> {
        navigateUp(it)
    }

    private fun navigateUp(it: FileData?) {
        context?.hideKeyboardFrom(binding.root.windowToken)
        val bundle = bundleOf(
            PARCELABLE_FILE_DATA_KEY to it, BOOLEAN_SHOULD_SCROLL_KEY to true
        )
        findNavController().navigate(
            R.id.action_addEditFileTagsFragment_to_fileMetadataFragment, bundle
        )
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnTagsFiltered().observe(this, onTagsFiltered)
        viewModel.getOnTagsUpdated().observe(this, onTagsUpdated)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnTagsFiltered().removeObserver(onTagsFiltered)
        viewModel.getOnTagsUpdated().removeObserver(onTagsUpdated)
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