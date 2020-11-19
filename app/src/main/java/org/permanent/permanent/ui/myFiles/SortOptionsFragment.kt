package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentSortOptionsBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.SortOptionsViewModel

const val PARCELABLE_SORT_OPTION_KEY = "parcelable_sort_option_key"
class SortOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentSortOptionsBinding
    private lateinit var viewModel: SortOptionsViewModel
    private val onSortRequest = MutableLiveData<SortType>()

    fun setBundleArguments(sortOption: SortType) {
        val bundle = Bundle()
        bundle.putString(PARCELABLE_SORT_OPTION_KEY, sortOption.toBackendString())
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSortOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(SortOptionsViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.setCurrentSortOption(arguments?.getString(PARCELABLE_SORT_OPTION_KEY))

        return binding.root
    }

    private val onSortRequestObserver = Observer<SortType> {
        dismiss()
        onSortRequest.value = it
    }

    fun getOnSortRequest(): MutableLiveData<SortType> {
        return onSortRequest
    }

    override fun connectViewModelEvents() {
        viewModel.getOnSortRequest().observe(this, onSortRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSortRequest().removeObserver(onSortRequestObserver)
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