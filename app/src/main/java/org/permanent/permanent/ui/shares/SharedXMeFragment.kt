package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentSharedXMeBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordOption
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.RecordOptionsFragment
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import org.permanent.permanent.viewmodels.SharedXMeViewModel

class SharedXMeFragment : PermanentBaseFragment(), DownloadableRecordListener {

    private lateinit var viewModel: SharedXMeViewModel
    private lateinit var binding: FragmentSharedXMeBinding
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: SharesAdapter
    private lateinit var downloadableRecord: DownloadableRecord
    private var recordOptionsFragment: RecordOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SharedXMeViewModel::class.java)
        binding = FragmentSharedXMeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setLifecycleOwner(this)
        initSharesRecyclerView(binding.rvShares)
        arguments?.takeIf { it.containsKey(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY) }?.apply {
            getString(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY).also { binding.tvNoShares.text = it }
        }
        arguments?.takeIf { it.containsKey(SHARED_WITH_ME_ITEM_LIST_KEY) }?.apply {
            getParcelableArrayList<DownloadableRecord>(SHARED_WITH_ME_ITEM_LIST_KEY).also {
                if (!it.isNullOrEmpty()) sharesAdapter.set(it)
                viewModel.existsShares.value = !it.isNullOrEmpty()
            }
        }
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onFileDownloadRequest = Observer<Record> {
        viewModel.download(downloadableRecord)
    }

    private fun initSharesRecyclerView(rvShares: RecyclerView) {
        sharesRecyclerView = rvShares
        sharesAdapter = SharesAdapter(this, this)
        sharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = sharesAdapter
        }
    }

    fun set(records: MutableList<DownloadableRecord>) {
        sharesAdapter.set(records)
        viewModel.existsShares.value = true
    }

    fun navigateToRecord(recordIdToNavigateTo: Int?) {
        recordIdToNavigateTo?.let {
            sharesAdapter.getItemPosition(it)?.let { position ->
                sharesRecyclerView.layoutManager?.scrollToPosition(position) }
        }
    }

    override fun onRecordOptionsClick(record: DownloadableRecord) {
        downloadableRecord = record
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(record,
            arrayListOf(RecordOption.COPY, RecordOption.MOVE, RecordOption.DELETE, RecordOption.SHARE))
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
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