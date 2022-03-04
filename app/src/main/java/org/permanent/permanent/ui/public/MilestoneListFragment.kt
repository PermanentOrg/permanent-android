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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentMilestoneListBinding
import org.permanent.permanent.models.Milestone
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.viewmodels.MilestoneListViewModel

class MilestoneListFragment : PermanentBaseFragment(), MilestoneListener {
    private lateinit var viewModel: MilestoneListViewModel
    private lateinit var binding: FragmentMilestoneListBinding
    private lateinit var milestonesRecyclerView: RecyclerView
    private lateinit var milestonesAdapter: MilestonesAdapter
    private var milestoneOptionsFragment: ItemOptionsFragment? = null
    private val onMilestonesRetrieved = Observer<List<Milestone>> {
        milestonesAdapter.set(it as MutableList<Milestone>)
    }

    private val onEditMilestoneFragment = Observer<Milestone> {
        onEditClick(it)
    }

    private val onDeleteMilestoneFragment = Observer<Milestone> {
        onDeleteClick(it)
    }

    private val onAddMilestoneRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(
            R.id.action_milestoneListFragment_to_addEditMilestoneFragment
        )
    }

    private val onShowMessage = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MilestoneListViewModel::class.java)
        binding = FragmentMilestoneListBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initMilestonesRecyclerView(binding.rvMilestones)

        return binding.root
    }

    private fun initMilestonesRecyclerView(rvMilestones: RecyclerView) {
        milestonesRecyclerView = rvMilestones
        milestonesAdapter = MilestonesAdapter(this)
        milestonesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = milestonesAdapter
        }
        milestonesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onOptionsClick(milestone: Milestone) {
        milestoneOptionsFragment = ItemOptionsFragment()
        milestoneOptionsFragment?.setBundleArguments(milestone)
        milestoneOptionsFragment?.show(
            parentFragmentManager,
            milestoneOptionsFragment?.tag
        )
        milestoneOptionsFragment?.getEditMilestoneRequest()
            ?.observe(this, onEditMilestoneFragment)
        milestoneOptionsFragment?.getDeleteMilestoneRequest()
            ?.observe(this, onDeleteMilestoneFragment)
    }

    override fun onEditClick(milestone: Milestone) {
        val bundle = bundleOf(PARCELABLE_MILESTONE_KEY to milestone)
        requireParentFragment().findNavController().navigate(
            R.id.action_milestoneListFragment_to_addEditMilestoneFragment,
            bundle
        )
    }

    override fun onDeleteClick(milestone: Milestone) {
        viewModel.deleteProfileItem(milestone)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getOnMilestonesRetrieved().observe(this, onMilestonesRetrieved)
        viewModel.getOnAddMilestoneRequest().observe(this, onAddMilestoneRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getOnMilestonesRetrieved().removeObserver(onMilestonesRetrieved)
        viewModel.getOnAddMilestoneRequest().removeObserver(onAddMilestoneRequest)
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
        const val PARCELABLE_MILESTONE_KEY = "parcelable_milestone_key"
    }
}