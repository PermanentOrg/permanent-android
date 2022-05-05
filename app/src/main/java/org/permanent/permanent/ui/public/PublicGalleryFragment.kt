package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicGalleryBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicGalleryViewModel

class PublicGalleryFragment: PermanentBaseFragment(), PublicArchiveListener  {
    private lateinit var viewModel: PublicGalleryViewModel
    private lateinit var binding: FragmentPublicGalleryBinding
    private lateinit var publicArchiveAdapter: PublicArchiveAdapter
    private lateinit var yourArchivesRecyclerView: RecyclerView

    private val onArchivesRetrieved = Observer<List<Archive>> {
        publicArchiveAdapter.set(it as MutableList<Archive>)
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(PublicGalleryViewModel::class.java)
        binding = FragmentPublicGalleryBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initYourArchivesRecyclerView(binding.rvYourPublicArchives)

        return binding.root
    }

    private fun initYourArchivesRecyclerView(rvYourPublicArchives: RecyclerView) {
        yourArchivesRecyclerView = rvYourPublicArchives
        publicArchiveAdapter = PublicArchiveAdapter(this)
        yourArchivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = publicArchiveAdapter
        }

    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnPublicArchivesRetrieved().observe(this, onArchivesRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnPublicArchivesRetrieved().removeObserver(onArchivesRetrieved)
    }

    override fun onArchiveClick(archive: Archive) {
        val bundle = bundleOf(ARCHIVE_NR to archive.number, ARCHIVE_NAME to archive.fullName)
        requireParentFragment().findNavController().navigate(R.id.action_publicGalleryFragment_to_publicFragment, bundle)
    }

    override fun onShareClick(archive: Archive) {
        viewModel.sharePublicArchive(archive)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getYourPublicArchives()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val ARCHIVE_NR = "archive_nr"
        const val ARCHIVE_NAME = "archive_name"
    }
}