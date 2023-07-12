package org.permanent.permanent.ui.public

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentArchiveSearchBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.showKeyboardFor
import org.permanent.permanent.viewmodels.ArchiveSearchViewModel

class ArchiveSearchFragment : PermanentBaseFragment(), PublicArchiveListener {
    private lateinit var binding: FragmentArchiveSearchBinding
    private lateinit var viewModel: ArchiveSearchViewModel
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var archivesAdapter: PublicArchiveAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArchiveSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ArchiveSearchViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initArchivesRecyclerView(binding.rvArchives)

        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    private val onArchivesRetrieved = Observer<List<Archive>> {
        archivesAdapter.set(it)
    }

    private fun initArchivesRecyclerView(rvArchives: RecyclerView) {
        archivesRecyclerView = rvArchives
        archivesAdapter = PublicArchiveAdapter(this)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = archivesAdapter
        }
    }

    override fun onArchiveClick(archive: Archive) {
        val bundle = bundleOf(PublicFragment.ARCHIVE to archive)
        requireParentFragment().findNavController()
            .navigate(R.id.action_archiveSearchFragment_to_publicFragment, bundle)
    }

    override fun onShareClick(archive: Archive) {
        context?.hideKeyboardFrom(binding.root.windowToken)
        viewModel.sharePublicArchive(archive)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnShowMessage().observe(this, onShowMessage)
        viewModel.getOnShowError().observe(this, onShowError)
        viewModel.getOnArchivesRetrieved().observe(this, onArchivesRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShowMessage().removeObserver(onShowMessage)
        viewModel.getOnShowError().removeObserver(onShowError)
        viewModel.getOnArchivesRetrieved().removeObserver(onArchivesRetrieved)
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