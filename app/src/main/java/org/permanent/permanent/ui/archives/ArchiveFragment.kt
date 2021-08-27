package org.permanent.permanent.ui.archives

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentArchiveBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.ArchiveViewModel

class ArchiveFragment : PermanentBaseFragment(), ArchiveListener, View.OnClickListener {

    private lateinit var binding: FragmentArchiveBinding
    private lateinit var viewModel: ArchiveViewModel
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var archivesAdapter: ArchivesAdapter
    private var archiveOptionsFragment: ArchiveOptionsFragment? = null

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onArchivesRetrieved = Observer<List<Archive>> {
        val prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        )
        archivesAdapter.set(it, prefsHelper.getDefaultArchiveId())
    }

    private val onChangeDefaultArchiveObserver = Observer<Int> {
        viewModel.changeDefaultArchiveTo(it)
    }

    private val onDefaultArchiveChanged = Observer<Int> {
        archivesAdapter.onDefaultArchiveChanged(it)
    }

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
        binding.btnCurrentArchiveOptions.setOnClickListener(this)
        initArchivesRecyclerView(binding.rvArchives)

        return binding.root
    }

    private fun initArchivesRecyclerView(rvArchives: RecyclerView) {
        archivesRecyclerView = rvArchives
        archivesAdapter = ArchivesAdapter(this)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = archivesAdapter
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCurrentArchiveOptions -> {
                val prefsHelper = PreferencesHelper(
                    requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                    )
                )

                showArchiveOptions(prefsHelper.getCurrentArchiveId())
            }
        }
    }

    override fun onArchiveClick(archive: Archive) {
        viewModel.switchCurrentArchiveTo(archive)
    }

    override fun onOptionsBtnClick(archive: Archive) {
        showArchiveOptions(archive.id)
    }

    private fun showArchiveOptions(currentArchiveId: Int) {
        archiveOptionsFragment = ArchiveOptionsFragment()
        archiveOptionsFragment?.setBundleArguments(currentArchiveId)
        archiveOptionsFragment?.show(parentFragmentManager, archiveOptionsFragment?.tag)
        archiveOptionsFragment?.getOnChangeDefaultArchiveRequest()
            ?.observe(this, onChangeDefaultArchiveObserver)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnArchivesRetrieved().observe(this, onArchivesRetrieved)
        viewModel.getOnDefaultArchiveChanged().observe(this, onDefaultArchiveChanged)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnArchivesRetrieved().removeObserver(onArchivesRetrieved)
        viewModel.getOnDefaultArchiveChanged().removeObserver(onDefaultArchiveChanged)
        archiveOptionsFragment?.getOnChangeDefaultArchiveRequest()
            ?.removeObserver(onChangeDefaultArchiveObserver)
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