package org.permanent.permanent.ui.archiveOnboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentArchiveOnboardingDefaultSelectionBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

class DefaultSelectionFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ArchiveOnboardingViewModel
    private lateinit var binding: FragmentArchiveOnboardingDefaultSelectionBinding
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var onboardingArchivesAdapter: OnboardingArchivesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ArchiveOnboardingViewModel::class.java]
        binding = FragmentArchiveOnboardingDefaultSelectionBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        initPendingArchivesRecyclerView(binding.rvArchives)

        return binding.root
    }

    private fun initPendingArchivesRecyclerView(rvPendingArchives: RecyclerView) {
        archivesRecyclerView = rvPendingArchives
        onboardingArchivesAdapter = OnboardingArchivesAdapter(viewModel)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = onboardingArchivesAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    private val archivesObserver = Observer<List<Archive>> {
        onboardingArchivesAdapter.set(it)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchivesRetrieved().observe(this, archivesObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchivesRetrieved().removeObserver(archivesObserver)
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