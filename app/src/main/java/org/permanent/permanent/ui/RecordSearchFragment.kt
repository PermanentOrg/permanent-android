package org.permanent.permanent.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentRecordSearchBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.fileView.FileActivity
import org.permanent.permanent.ui.fileView.FileInfoFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.ui.myFiles.RecordsListAdapter
import org.permanent.permanent.viewmodels.RecordSearchViewModel

class RecordSearchFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentRecordSearchBinding
    private lateinit var viewModel: RecordSearchViewModel
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsListAdapter: RecordsListAdapter
    private var recordToView: Record? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(RecordSearchViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initRecordsRecyclerView(binding.rvRecords)

        return binding.root
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    private val onVisibleTagsReady = Observer<ArrayList<Tag>> {
        val chipGroup = binding.chipGroupTags
        chipGroup.removeAllViews()

        it.forEachIndexed { index, tag ->
            val chip = layoutInflater.inflate(
                R.layout.item_chip_filter_purple, chipGroup, false
            ) as Chip
            chip.text = (tag.name)

            chip.isChecked = tag.isCheckedOnLocal
            chip.setEnsureMinTouchTargetSize(false)
            chip.setOnCheckedChangeListener { _, isChecked ->
                tag.isCheckedOnLocal = isChecked
                viewModel.currentSearchQuery.value = ""
                viewModel.searchRecords()
            }
            if (index == 0) {
                val layoutParams = chip.layoutParams as ChipGroup.LayoutParams
                layoutParams.marginStart = 40
                chip.layoutParams = layoutParams
            }
            if (index == it.lastIndex) {
                val layoutParams = chip.layoutParams as ChipGroup.LayoutParams
                layoutParams.marginEnd = 40
                chip.layoutParams = layoutParams
            }
            chipGroup.addView(chip)
        }
    }

    private val onRecordsRetrieved = Observer<List<Record>> {
        recordsListAdapter.setRecords(it)
    }

    private val onFileViewRequest = Observer<ArrayList<Record>> {
        for (record in it) {
            if (record.displayFirstInCarousel) recordToView = record
        }
        val intent = Intent(context, FileActivity::class.java)
        intent.putExtra(PARCELABLE_FILES_KEY, it)
        startActivityForResult(intent, FileInfoFragment.ACTIVITY_RESULT_REQUEST_CODE)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileInfoFragment.ACTIVITY_RESULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val returnValue = data?.getStringExtra(FileInfoFragment.ACTIVITY_RESULT_FILE_NAME_KEY)
            recordsListAdapter.updateNameOfRecord(recordToView?.id, returnValue)
        }
    }

    private fun initRecordsRecyclerView(rvFiles: RecyclerView) {
        recordsRecyclerView = rvFiles
        recordsListAdapter = RecordsListAdapter(
            this, false, MutableLiveData(false),
            isForSharesScreen = false,
            isForSearchScreen = true,
            recordListener = viewModel
        )
        recordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsListAdapter
            setHasFixedSize(true)
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnShowMessage().observe(this, onShowMessage)
        viewModel.getOnVisibleTagsReady().observe(this, onVisibleTagsReady)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShowMessage().removeObserver(onShowMessage)
        viewModel.getOnVisibleTagsReady().removeObserver(onVisibleTagsReady)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        binding.etSearchQuery.requestFocus()
        context?.showKeyboardFor(binding.etSearchQuery)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}