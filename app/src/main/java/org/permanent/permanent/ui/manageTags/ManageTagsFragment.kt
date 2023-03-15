package org.permanent.permanent.ui.manageTags

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.ManageTagsViewModel

class ManageTagsFragment : PermanentBaseFragment() {
    private lateinit var tagAdapter: ManageTagsAdapter

    companion object {
        fun newInstance() = ManageTagsFragment()
    }

    private lateinit var viewModel: ManageTagsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ManageTagsViewModel::class.java)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.tagsRV)
        tagAdapter = ManageTagsAdapter(emptyList())
        recyclerView?.adapter = tagAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
    }

    override fun connectViewModelEvents() {
        viewModel.getTags().observe(this, onTags)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getTags().removeObserver(onTags)
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    private val onTags = Observer<List<Tag>> {
        tagAdapter = ManageTagsAdapter(it)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.tagsRV)
        recyclerView?.adapter = tagAdapter
    }
}