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
import org.permanent.permanent.databinding.FragmentArchiveOnboardingPendingInvitationsBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

class PendingInvitationsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ArchiveOnboardingViewModel
    private lateinit var binding: FragmentArchiveOnboardingPendingInvitationsBinding
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var pendingArchivesRecyclerView: RecyclerView
    private lateinit var onboardingArchivesAdapter: OnboardingArchivesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ArchiveOnboardingViewModel::class.java]
        binding = FragmentArchiveOnboardingPendingInvitationsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        initPendingArchivesRecyclerView(binding.rvPendingArchives)

        return binding.root
    }

    private fun initPendingArchivesRecyclerView(rvPendingArchives: RecyclerView) {
        pendingArchivesRecyclerView = rvPendingArchives
        onboardingArchivesAdapter = OnboardingArchivesAdapter(viewModel)
        pendingArchivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = onboardingArchivesAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    private val pendingArchivesObserver = Observer<List<Archive>> {
        onboardingArchivesAdapter.set(it)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchivesRetrieved().observe(this, pendingArchivesObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchivesRetrieved().removeObserver(pendingArchivesObserver)
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