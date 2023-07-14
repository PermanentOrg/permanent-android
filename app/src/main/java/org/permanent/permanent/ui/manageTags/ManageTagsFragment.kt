package org.permanent.permanent.ui.manageTags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.databinding.FragmentManageTagsBinding
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.addEditTag.AddEditTagFragment
import org.permanent.permanent.viewmodels.ManageTagsViewModel

class ManageTagsFragment : PermanentBaseFragment(), ManageTagListener {
    private lateinit var binding: FragmentManageTagsBinding
    private lateinit var tagAdapter: ManageTagsAdapter
    private lateinit var viewModel: ManageTagsViewModel

    private var addTagFragment: AddEditTagFragment? = null

    companion object {
        const val PARCELABLE_TAG_KEY = "parcelable_tag_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageTagsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ManageTagsViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private val onAddButtonEvent = Observer<Void?> {
        addTagFragment = AddEditTagFragment()
        addTagFragment?.show(parentFragmentManager, addTagFragment?.tag)
        addTagFragment?.didUpdateTag?.observe(this, onDidUpdateTag)
    }

    private val onDidUpdateTag = Observer<Void?> {
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
        // Showing confirmationDialog
        val dialogBinding: DialogTitleTextTwoButtonsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_title_text_two_buttons, null, false
        )
        val alert = android.app.AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.delete_tags_title)
        dialogBinding.tvText.text = getString(R.string.delete_tags_text)
        dialogBinding.btnPositive.text = getString(R.string.delete_button)
        dialogBinding.btnPositive.setOnClickListener {
            viewModel.deleteTag(tag)
            alert.dismiss()
        }
        dialogBinding.btnNegative.text = getString(R.string.button_cancel)
        dialogBinding.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }
}