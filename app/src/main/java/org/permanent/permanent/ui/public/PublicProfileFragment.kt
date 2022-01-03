package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentPublicProfileBinding
import org.permanent.permanent.models.Milestone
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class PublicProfileFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentPublicProfileBinding
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var milestonesRecyclerView: RecyclerView
    private lateinit var milestonesAdapter: MilestonesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(PublicProfileViewModel::class.java)
        binding = FragmentPublicProfileBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initMilestonesRecyclerView(binding.rvMilestones)

        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onMilestonesRetrieved = Observer<MutableList<Milestone>> {
        milestonesAdapter.set(it)
    }

    private val onReadMoreAbout = Observer<Void> {
        binding.tvAboutText.maxLines = MAX_LINES_NO_LIMIT
    }

    private val onReadLessAbout = Observer<Void> {
        binding.tvAboutText.maxLines = MAX_LINES_ABOUT
    }

    private val onReadMoreOnlinePresence = Observer<Void> {
        binding.tvOnlinePresenceText.maxLines = MAX_LINES_NO_LIMIT
    }

    private val onReadLessOnlinePresence = Observer<Void> {
        binding.tvOnlinePresenceText.maxLines = MAX_LINES_ONLINE_PRESENCE
    }

    private fun initMilestonesRecyclerView(rvMilestones: RecyclerView) {
        milestonesRecyclerView = rvMilestones
        milestonesAdapter = MilestonesAdapter()
        milestonesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = milestonesAdapter
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnMilestonesRetrieved().observe(this, onMilestonesRetrieved)
        viewModel.getOnReadMoreAbout().observe(this, onReadMoreAbout)
        viewModel.getOnReadLessAbout().observe(this, onReadLessAbout)
        viewModel.getOnReadMoreOnlinePresence().observe(this, onReadMoreOnlinePresence)
        viewModel.getOnReadLessOnlinePresence().observe(this, onReadLessOnlinePresence)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnMilestonesRetrieved().removeObserver(onMilestonesRetrieved)
        viewModel.getOnReadMoreAbout().removeObserver(onReadMoreAbout)
        viewModel.getOnReadLessAbout().removeObserver(onReadLessAbout)
        viewModel.getOnReadMoreOnlinePresence().removeObserver(onReadMoreOnlinePresence)
        viewModel.getOnReadLessOnlinePresence().removeObserver(onReadLessOnlinePresence)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val MAX_LINES_ABOUT = 5
        const val MAX_LINES_ONLINE_PRESENCE = 3
        const val MAX_LINES_NO_LIMIT = 900
    }
}
