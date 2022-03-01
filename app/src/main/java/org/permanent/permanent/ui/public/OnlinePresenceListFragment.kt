package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentOnlinePresenceListBinding
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.viewmodels.OnlinePresenceListViewModel

class OnlinePresenceListFragment: PermanentBaseFragment(), OnlinePresenceListener {
    private lateinit var viewModel: OnlinePresenceListViewModel
    private lateinit var binding: FragmentOnlinePresenceListBinding
    private lateinit var onlinePresenceRecyclerView: RecyclerView
    private lateinit var onlinePresenceListAdapter: OnlinePresenceListAdapter
    private var onlinePresenceOptionsFragment: ItemOptionsFragment? = null
    private val onOnlinePresencesRetrieved = Observer<List<ProfileItem>> {
        onlinePresenceListAdapter.set(it as MutableList<ProfileItem>)
    }

    private val onShowOnlinePresenceEditFragment = Observer<ProfileItem> {
        onEditClick(it)
    }

    private val onProfileItemDeleteRequest = Observer<ProfileItem> {
        onDeleteClick(it)
    }

    private val onAddRequest = Observer<Boolean> {
        val bundle = bundleOf(IS_ADD_EMAIL to it)
        requireParentFragment().findNavController().navigate(R.id.action_onlinePresenceListFragment_to_addSocialMediaFragment, bundle)
        if(it == true) {
            (activity as AppCompatActivity?)?.supportActionBar?.title = getString(
                R.string.add_email_button
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(OnlinePresenceListViewModel::class.java)
        binding = FragmentOnlinePresenceListBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initOnlinePresencesRecyclerView(binding.rvOnlinePresence)

        return binding.root
    }

    private fun initOnlinePresencesRecyclerView(rvOnlinePresence: RecyclerView) {
        onlinePresenceRecyclerView = rvOnlinePresence
        onlinePresenceListAdapter = OnlinePresenceListAdapter(this)
        onlinePresenceRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = onlinePresenceListAdapter
        }
        onlinePresenceRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onOptionsClick(profileItem: ProfileItem) {
        onlinePresenceOptionsFragment = ItemOptionsFragment()
        onlinePresenceOptionsFragment?.setBundleArguments(profileItem)
        onlinePresenceOptionsFragment?.show(
            parentFragmentManager,
            onlinePresenceOptionsFragment?.tag
        )
        onlinePresenceOptionsFragment?.getShowEditOnlinePresenceFragmentRequest()
            ?.observe(this, onShowOnlinePresenceEditFragment)
        onlinePresenceOptionsFragment?.getDeleteOnlinePresenceRequest()
            ?.observe(this, onProfileItemDeleteRequest)
    }

    override fun onEditClick(profileItem: ProfileItem) {
        val bundle =
            bundleOf(PARCELABLE_PROFILE_ITEMS_KEY to profileItem, IS_EDIT_ONLINE_PRESENCE to true)
        requireParentFragment().findNavController()
            .navigate(R.id.action_onlinePresenceListFragment_to_addSocialMediaFragment, bundle)
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(
            R.string.edit_online_presence_label
        )

    }

    override fun onDeleteClick(profileItem: ProfileItem) {
        viewModel.deleteProfileItem(profileItem)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnOnlinePresencesRetrieved().observe(this, onOnlinePresencesRetrieved)
        viewModel.getOnAddRequest().observe(this, onAddRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnOnlinePresencesRetrieved().removeObserver(onOnlinePresencesRetrieved)
        viewModel.getOnAddRequest().removeObserver(onAddRequest)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProfileItems()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val PARCELABLE_PROFILE_ITEMS_KEY = "parcelable_profile_items_key"
        const val IS_EDIT_ONLINE_PRESENCE = "is_edit_online_presence"
        const val IS_ADD_EMAIL = "is_add_email"
    }
}