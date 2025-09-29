package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSelectionOptionsBinding
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.viewmodels.SelectionOptionsViewModel

class SelectionOptionsFragment : PermanentBottomSheetFragment(), View.OnClickListener {
    private lateinit var binding: FragmentSelectionOptionsBinding
    private lateinit var viewModel: SelectionOptionsViewModel
    private val onModifyRequest = MutableLiveData<ModificationType>()

    fun setBundleArguments(pair: Pair<Int, Boolean>) {
        val bundle = Bundle()
        bundle.putInt(SELECTION_SIZE_KEY, pair.first)
        bundle.putBoolean(SELECTION_CONTAINS_FOLDER_KEY, pair.second)
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
        binding.btnEdit.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        viewModel.setSelectionSize(arguments?.getInt(SELECTION_SIZE_KEY))
        val isSelectionContainingFolders = arguments?.getBoolean(SELECTION_CONTAINS_FOLDER_KEY)
        if (isSelectionContainingFolders == true) binding.btnEdit.visibility = View.GONE

        return binding.root
    }

    override fun onClick(view: View) {
        dismiss()
        when (view.id) {
            R.id.btnCopy -> onModifyRequest.value = ModificationType.COPY
            R.id.btnMove -> onModifyRequest.value = ModificationType.MOVE
            R.id.btnEdit -> onModifyRequest.value = ModificationType.EDIT
            R.id.btnDelete -> onModifyRequest.value = ModificationType.DELETE
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

    fun getOnSelectionModifyRequest(): MutableLiveData<ModificationType> = onModifyRequest

    companion object {
        const val SELECTION_SIZE_KEY = "selection_size_key"
        const val SELECTION_CONTAINS_FOLDER_KEY = "selection_contains_folder_key"
    }
}