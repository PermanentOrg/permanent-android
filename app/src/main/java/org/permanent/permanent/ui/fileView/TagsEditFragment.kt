package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentTagsEditBinding
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.TagsEditViewModel

class TagsEditFragment : PermanentBaseFragment() {

    private lateinit var viewModel: TagsEditViewModel
    private lateinit var binding: FragmentTagsEditBinding
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(TagsEditViewModel::class.java)
        binding = FragmentTagsEditBinding.inflate(inflater, container, false)
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
                    navigateUp(viewModel.getCurrentFileData())
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
                // TODO
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val onTagsRetrieved = Observer<List<Tag>> { archiveTags ->
        val chipGroup = binding.chipGroupAllTags
        for (archiveTag in archiveTags) {
            val chip = layoutInflater.inflate(
                R.layout.item_chip_filter, chipGroup, false) as Chip
            chip.text = (archiveTag.name)
            chip.isChecked = fileData?.tags?.contains(archiveTag) == true
            chipGroup.addView(chip)
        }
    }

    private fun navigateUp(it: FileData?) {
        val bundle = bundleOf(
            PARCELABLE_FILE_DATA_KEY to it, BOOLEAN_SHOULD_SCROLL_KEY to true
        )
        findNavController().navigate(
            R.id.action_tagsEditFragment_to_fileMetadataFragment, bundle
        )
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnTagsRetrieved().observe(this, onTagsRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnTagsRetrieved().removeObserver(onTagsRetrieved)
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