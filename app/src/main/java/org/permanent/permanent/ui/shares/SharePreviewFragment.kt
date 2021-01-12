package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentSharePreviewBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.RecordsGridAdapter
import org.permanent.permanent.viewmodels.SharePreviewViewModel

const val URL_TOKEN_KEY = "urlToken"

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

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
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
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getShowMessage().removeObserver(onShowMessage)
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