package org.permanent.permanent.ui.manageTags

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
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.addEditTag.AddEditTagFragment
import org.permanent.permanent.viewmodels.ManageTagsViewModel

class ManageTagsFragment : PermanentBaseFragment(), ManageTagListener {
    private lateinit var tagAdapter: ManageTagsAdapter

    private var addTagFragment: AddEditTagFragment? = null

    companion object {
        const val PARCELABLE_TAG_KEY = "parcelable_tag_key"
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
        tagAdapter = ManageTagsAdapter(emptyList(), this)
        recyclerView?.adapter = tagAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        val fab = view?.findViewById<View>(R.id.fabAdd2)
        fab?.bringToFront()
        fab?.setOnClickListener {
            viewModel.onAddButtonPressed()
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getTags().observe(this, onTags)
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnAddButtonEvent().observe(this, onAddButtonEvent)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getTags().removeObserver(onTags)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnAddButtonEvent().removeObserver(onAddButtonEvent)
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
        tagAdapter = ManageTagsAdapter(it, this)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.tagsRV)
        recyclerView?.adapter = tagAdapter
    }

    private val onAddButtonEvent = Observer<Void> {
        addTagFragment = AddEditTagFragment()
        addTagFragment?.show(parentFragmentManager, addTagFragment?.tag)
        addTagFragment?.didUpdateTag?.observe(this, onDidUpdateTag)
    }

    private val onDidUpdateTag = Observer<Void> {
        viewModel.reloadTags()

        removeOnDidUpdateTagObserver()
    }

    private fun removeOnDidUpdateTagObserver() {
        addTagFragment?.didUpdateTag?.removeObserver(onDidUpdateTag)
    }

    override fun onTagEditClicked(tag: Tag) {
        addTagFragment = AddEditTagFragment()
        addTagFragment?.show(parentFragmentManager, addTagFragment?.tag)
        addTagFragment?.setBundleArguments(tag)
        addTagFragment?.didUpdateTag?.observe(this, onDidUpdateTag)
    }

    override fun onTagDeleteClicked(tag: Tag) {
        viewModel.deleteTag(tag)
    }
}