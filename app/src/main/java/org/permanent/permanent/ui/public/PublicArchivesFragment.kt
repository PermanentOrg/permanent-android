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
import org.permanent.permanent.databinding.FragmentPublicArchivesBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicArchivesViewModel

class PublicArchivesFragment: PermanentBaseFragment(), PublicArchiveListener  {
    private lateinit var viewModel: PublicArchivesViewModel
    private lateinit var binding: FragmentPublicArchivesBinding
    private lateinit var publicArchiveAdapter: PublicArchiveAdapter
    private lateinit var yourGalleriesRecyclerView: RecyclerView

    private val onGalleriesRetrieved = Observer<List<Archive>> {
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
        viewModel = ViewModelProvider(this).get(PublicArchivesViewModel::class.java)
        binding = FragmentPublicArchivesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initYourGalleriesRecyclerView(binding.rvYourPublicGalleries)

        return binding.root
    }

    private fun initYourGalleriesRecyclerView(rvYourPublicGalleries: RecyclerView) {
        yourGalleriesRecyclerView = rvYourPublicGalleries
        publicArchiveAdapter = PublicArchiveAdapter(this)
        yourGalleriesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = publicArchiveAdapter
        }

    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnGalleriesRetrieved().observe(this, onGalleriesRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnGalleriesRetrieved().removeObserver(onGalleriesRetrieved)
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
        viewModel.getYourPublicGalleries()
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