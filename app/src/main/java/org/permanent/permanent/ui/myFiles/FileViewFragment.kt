package org.permanent.permanent.ui.myFiles

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileViewBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileViewViewModel

const val PARCELABLE_FILE_DATA_KEY = "parcelable_file_data_key"
class FileViewFragment : PermanentBaseFragment() {

    private lateinit var viewModel: FileViewViewModel
    private lateinit var binding: FragmentFileViewBinding
    private lateinit var supportActionBar: ActionBar
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileViewViewModel::class.java)
        binding = FragmentFileViewBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
        }
        supportActionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        supportActionBar.title = fileData?.displayName
        supportActionBar.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.black)))
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        supportActionBar.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorPrimary)))
    }
}