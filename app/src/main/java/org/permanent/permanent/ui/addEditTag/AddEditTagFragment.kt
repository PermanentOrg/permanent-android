package org.permanent.permanent.ui.addEditTag

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import org.permanent.permanent.databinding.FragmentAddEditTagBinding
import org.permanent.permanent.models.Tag
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.manageTags.ManageTagsFragment
import org.permanent.permanent.viewmodels.AddEditTagViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class AddEditTagFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentAddEditTagBinding
    val didUpdateTag = SingleLiveEvent<Void>()

    companion object {
        fun newInstance() = AddEditTagFragment()
    }

    private lateinit var viewModel: AddEditTagViewModel

    fun setBundleArguments(
        tag: Tag,
    ) {
        val bundle = Bundle()
        bundle.putParcelable(ManageTagsFragment.PARCELABLE_TAG_KEY, tag)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditTagBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AddEditTagViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.setTag(arguments?.getParcelable(ManageTagsFragment.PARCELABLE_TAG_KEY))

        return binding.root
    }

    override fun connectViewModelEvents() {
        viewModel.getOnDismissEvent().observe(this, onDismissEvent)
        viewModel.getOnUpdateSuccessEvent().observe(this, onUpdateSuccessEvent)
        viewModel.getOnUpdateFailedEvent().observe(this, onUpdateFailedEvent)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnDismissEvent().removeObserver(onDismissEvent)
        viewModel.getOnUpdateSuccessEvent().removeObserver(onUpdateSuccessEvent)
        viewModel.getOnUpdateFailedEvent().removeObserver(onUpdateFailedEvent)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    private val onDismissEvent = Observer<Void> {
        dismiss()
    }

    private val onUpdateSuccessEvent = Observer<Void> {
        didUpdateTag.call()
        dismiss()
    }

    private val onUpdateFailedEvent = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

}