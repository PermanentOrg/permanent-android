package org.permanent.permanent.ui.archives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentArchiveBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.ArchiveViewModel

class ArchiveFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentArchiveBinding
    private lateinit var viewModel: ArchiveViewModel
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var archivesAdapter: ArchivesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ArchiveViewModel::class.java)
        binding = FragmentArchiveBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initArchivesRecyclerView(binding.rvArchives)

        return binding.root
    }

    private fun initArchivesRecyclerView(rvArchives: RecyclerView) {
        archivesRecyclerView = rvArchives
        archivesAdapter = ArchivesAdapter(viewModel)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = archivesAdapter
        }
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onArchivesRetrieved = Observer<List<Archive>> {
        archivesAdapter.set(it)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnArchivesRetrieved().observe(this, onArchivesRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnArchivesRetrieved().removeObserver(onArchivesRetrieved)
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