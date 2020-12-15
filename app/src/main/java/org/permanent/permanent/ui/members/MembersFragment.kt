package org.permanent.permanent.ui.members

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.databinding.FragmentMembersBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.MembersViewModel

const val SNACKBAR_DURATION_MILLIS = 4000

class MembersFragment : PermanentBaseFragment() {

    private lateinit var viewModel: MembersViewModel
    private lateinit var binding: FragmentMembersBinding
    private lateinit var ownersRecyclerView: RecyclerView
    private lateinit var curatorsRecyclerView: RecyclerView
    private lateinit var editorsRecyclerView: RecyclerView
    private lateinit var contributorsRecyclerView: RecyclerView
    private lateinit var viewersRecyclerView: RecyclerView
    private lateinit var ownersAdapter: MembersAdapter
    private lateinit var curatorsAdapter: MembersAdapter
    private lateinit var editorsAdapter: MembersAdapter
    private lateinit var contributorsAdapter: MembersAdapter
    private lateinit var viewersAdapter: MembersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MembersViewModel::class.java)
        binding = FragmentMembersBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initOwnersRecyclerView(binding.rvOwners)
        initCuratorsRecyclerView(binding.rvCurators)
        initEditorsRecyclerView(binding.rvEditors)
        initContributorsRecyclerView(binding.rvContributors)
        initViewersRecyclerView(binding.rvViewers)

        return binding.root
    }

    private fun initOwnersRecyclerView(rvOwners: RecyclerView) {
        ownersRecyclerView = rvOwners
        ownersAdapter = MembersAdapter()
        ownersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = ownersAdapter
        }
    }

    private fun initCuratorsRecyclerView(rvCurators: RecyclerView) {
        curatorsRecyclerView = rvCurators
        curatorsAdapter = MembersAdapter()
        curatorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = curatorsAdapter
        }
    }

    private fun initEditorsRecyclerView(rvEditors: RecyclerView) {
        editorsRecyclerView = rvEditors
        editorsAdapter = MembersAdapter()
        editorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = editorsAdapter
        }
    }

    private fun initContributorsRecyclerView(rvContributors: RecyclerView) {
        contributorsRecyclerView = rvContributors
        contributorsAdapter = MembersAdapter()
        contributorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contributorsAdapter
        }
    }

    private fun initViewersRecyclerView(rvViewers: RecyclerView) {
        viewersRecyclerView = rvViewers
        viewersAdapter = MembersAdapter()
        viewersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = viewersAdapter
        }
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("WrongConstant")
    private val onShowSnackbar = Observer<Int> { messageInt ->
        Snackbar.make(binding.root, messageInt, SNACKBAR_DURATION_MILLIS).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowSnackbar().observe(this, onShowSnackbar)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowSnackbar().removeObserver(onShowSnackbar)
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