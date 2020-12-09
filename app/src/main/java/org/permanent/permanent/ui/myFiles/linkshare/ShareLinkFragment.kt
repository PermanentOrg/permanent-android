package org.permanent.permanent.ui.myFiles.linkshare

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentShareLinkBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.ShareLinkViewModel


class ShareLinkFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ShareLinkViewModel
    private lateinit var binding: FragmentShareLinkBinding
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var archivesAdapter: ArchivesAdapter
    private var record: Record? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ShareLinkViewModel::class.java)
        binding = FragmentShareLinkBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        record?.let {
            viewModel.setRecord(it)
            initArchivesRecyclerView(binding.rvArchives, it)
        }
        return binding.root
    }

    private fun initArchivesRecyclerView(rvArchives: RecyclerView, record: Record) {
        archivesRecyclerView = rvArchives
        archivesAdapter = ArchivesAdapter(record.shares)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = archivesAdapter
        }
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onRevokeLinkRequest = Observer<Void> {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_delete, null)
        val alert = AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.tvTitle.text = getString(R.string.share_link_revoke_title)
        viewDialog.btnDelete.text = getString(R.string.share_link_revoke_button)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.deleteShareLink()
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onErrorMessage)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onErrorMessage)
        viewModel.getOnRevokeLinkRequest().removeObserver(onRevokeLinkRequest)
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