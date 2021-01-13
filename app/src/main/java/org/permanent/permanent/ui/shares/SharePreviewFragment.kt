package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSharePreviewBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.RecordsGridAdapter
import org.permanent.permanent.viewmodels.SharePreviewViewModel

const val URL_TOKEN_KEY = "url_token"
const val RECORD_TO_NAVIGATE_TO_KEY = "record_to_navigate_to"
const val SELECTED_FRAGMENT_POSITION_KEY = "selected_fragment"

class SharePreviewFragment : PermanentBaseFragment() {

    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsGridAdapter
    private lateinit var binding: FragmentSharePreviewBinding
    private lateinit var viewModel: SharePreviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SharePreviewViewModel::class.java)
        binding = FragmentSharePreviewBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        arguments?.takeIf { it.containsKey(URL_TOKEN_KEY) }?.apply {
            val urlToken = getString(URL_TOKEN_KEY)

            if (!urlToken.isNullOrEmpty()) viewModel.checkShareLink(urlToken)
        }
        initRecordsRecyclerView(binding.rvRecords)
        return binding.root
    }

    private val onRecordsRetrieved = Observer<List<Record>> {
        recordsAdapter.set(it)
    }

    private val onViewInArchive = Observer<Void> {
        val bundle = bundleOf(SELECTED_FRAGMENT_POSITION_KEY
                to Constants.POSITION_SHARED_WITH_ME_FRAGMENT)
        findNavController().navigate(R.id.action_sharePreviewFragment_to_sharesFragment, bundle)
    }

    private val onNavigateUp = Observer<Void> {
        findNavController().navigateUp()
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsAdapter = RecordsGridAdapter(this)
        recordsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            adapter = recordsAdapter
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnViewInArchive().observe(this, onViewInArchive)
        viewModel.getOnNavigateUp().observe(this, onNavigateUp)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnViewInArchive().removeObserver(onViewInArchive)
        viewModel.getOnNavigateUp().removeObserver(onNavigateUp)
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