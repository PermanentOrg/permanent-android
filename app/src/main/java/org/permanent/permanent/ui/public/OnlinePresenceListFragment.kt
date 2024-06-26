package org.permanent.permanent.ui.public

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentOnlinePresenceListBinding
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.ui.public.PublicProfileFragment.Companion.PARCELABLE_PROFILE_ITEM_KEY
import org.permanent.permanent.viewmodels.OnlinePresenceListViewModel

class OnlinePresenceListFragment : PermanentBaseFragment(), ProfileItemListener {
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

    private val onAddRequest = Observer<Boolean> { isAddEmail ->
        val bundle = bundleOf(IS_ADD_EMAIL to isAddEmail)
        requireParentFragment().findNavController().navigate(
            R.id.action_onlinePresenceListFragment_to_addEditOnlinePresenceFragment,
            bundle
        )
        if (isAddEmail) (activity as AppCompatActivity?)?.supportActionBar?.title = getString(
                R.string.add_email_button
            )
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[OnlinePresenceListViewModel::class.java]
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
        onlinePresenceOptionsFragment?.getEditProfileItemRequest()
            ?.observe(this, onShowOnlinePresenceEditFragment)
        onlinePresenceOptionsFragment?.getDeleteProfileItemRequest()
            ?.observe(this, onProfileItemDeleteRequest)
    }

    override fun onEditClick(profileItem: ProfileItem) {
        val bundle = bundleOf(
            PARCELABLE_PROFILE_ITEM_KEY to profileItem,
            IS_EDIT_ONLINE_PRESENCE to true
        )
        requireParentFragment().findNavController().navigate(
            R.id.action_onlinePresenceListFragment_to_addEditOnlinePresenceFragment,
            bundle
        )
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(
            R.string.edit_online_presence_label
        )
    }

    override fun onDeleteClick(profileItem: ProfileItem) {
        viewModel.deleteProfileItem(profileItem)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getOnOnlinePresencesRetrieved().observe(this, onOnlinePresencesRetrieved)
        viewModel.getOnAddRequest().observe(this, onAddRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
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
        const val IS_EDIT_ONLINE_PRESENCE = "is_edit_online_presence"
        const val IS_ADD_EMAIL = "is_add_email"
    }
}