package org.permanent.permanent.ui.myFiles.linkshare

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentShareLinkBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.ShareLinkViewModel

const val PARCELABLE_SHARE_KEY = "parcelable_share_key"

class ShareLinkFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ShareLinkViewModel
    private lateinit var binding: FragmentShareLinkBinding
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: SharesAdapter
    private var record: Record? = null
    private var itemOptionsFragment: ItemOptionsFragment? = null

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
            initSharesRecyclerView(binding.rvShares, it)
        }
        return binding.root
    }

    private fun initSharesRecyclerView(rvShares: RecyclerView, record: Record) {
        sharesRecyclerView = rvShares
        sharesAdapter = SharesAdapter(this, record.shares, viewModel)
        sharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = sharesAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    private val showMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val showSnackBar = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onLinkSettingsRequest = Observer<ShareByUrl> {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record, PARCELABLE_SHARE_KEY to it)
        findNavController().navigate(R.id.action_shareLinkFragment_to_linkSettingsFragment, bundle)
    }

    private val onShowShareOptionsObserver = Observer<Share> { share ->
        itemOptionsFragment = ItemOptionsFragment()
        itemOptionsFragment?.setBundleArguments(share)
        itemOptionsFragment?.show(parentFragmentManager, itemOptionsFragment?.tag)
        itemOptionsFragment?.getOnShareRemoved()?.observe(this, onShareRemoved)
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

    private val onShareRemoved = Observer<Share> {
        sharesAdapter.remove(it)
        record?.shares?.remove(it)
        viewModel.getExistsShares().value = !record?.shares.isNullOrEmpty()
    }

    private val onShowEditShareDialog = Observer<Share> {

    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, showMessage)
        viewModel.getShowSnackBar().observe(this, showSnackBar)
        viewModel.getOnLinkSettingsRequest().observe(this, onLinkSettingsRequest)
        viewModel.getOnShowShareOptionsRequest().observe(this, onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
        viewModel.getOnShareDenied().observe(this, onShareRemoved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(showMessage)
        viewModel.getShowSnackBar().removeObserver(showSnackBar)
        viewModel.getOnLinkSettingsRequest().removeObserver(onLinkSettingsRequest)
        viewModel.getOnShowShareOptionsRequest().removeObserver(onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().removeObserver(onRevokeLinkRequest)
        viewModel.getOnShareDenied().removeObserver(onShareRemoved)
        itemOptionsFragment?.getOnShareRemoved()?.removeObserver(onShareRemoved)
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