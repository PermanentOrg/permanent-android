package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSelectionOptionsBinding
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.viewmodels.SelectionOptionsViewModel

class SelectionOptionsFragment : PermanentBottomSheetFragment(), View.OnClickListener {
    private lateinit var binding: FragmentSelectionOptionsBinding
    private lateinit var viewModel: SelectionOptionsViewModel
    private val onRelocateRequest = MutableLiveData<RelocationType>()

    fun setBundleArguments(selectionSize: Int) {
        val bundle = Bundle()
        bundle.putInt(SELECTION_SIZE_KEY, selectionSize)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SelectionOptionsViewModel::class.java]
        binding = FragmentSelectionOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.btnCopy.setOnClickListener(this)
        binding.btnMove.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        viewModel.setSelectionSize(arguments?.getInt(SELECTION_SIZE_KEY))
        return binding.root
    }

    override fun onClick(view: View) {
        dismiss()
        when (view.id) {
            R.id.btnCopy -> {
                onRelocateRequest.value = RelocationType.COPY
            }
            R.id.btnMove -> {
                onRelocateRequest.value = RelocationType.MOVE
            }
            R.id.btnDelete -> {
                onRelocateRequest.value = RelocationType.DELETE
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnSelectionRelocateRequest(): MutableLiveData<RelocationType> = onRelocateRequest

    companion object {
        const val SELECTION_SIZE_KEY = "selection_size_key"
    }
}