package org.permanent.permanent.ui.myFiles.linkshare

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentShareLinkBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.ShareLinkViewModel

const val PARCELABLE_SHARE_KEY = "parcelable_share_key"

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

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onShowSnackBar = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = 232
        view.layoutParams = params
        snackBar.show()
    }

    private val onManageLinkRequest = Observer<ShareByUrl> {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record, PARCELABLE_SHARE_KEY to it)
        findNavController().navigate(R.id.action_shareLinkFragment_to_manageLinkFragment, bundle)
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
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowSnackBar().observe(this, onShowSnackBar)
        viewModel.getOnManageLinkRequest().observe(this, onManageLinkRequest)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowSnackBar().removeObserver(onShowSnackBar)
        viewModel.getOnManageLinkRequest().removeObserver(onManageLinkRequest)
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