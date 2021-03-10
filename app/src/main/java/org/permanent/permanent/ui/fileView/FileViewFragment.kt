package org.permanent.permanent.ui.fileView

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileViewViewModel::class.java)
        binding = FragmentFileViewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
        }
        binding.executePendingBindings()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_file_view, menu)
        super.onCreateOptionsMenu(menu, inflater)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.metadataItem -> {
                val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
                findNavController().navigate(R.id.action_fileViewFragment_to_fileMetadataFragment, bundle)
                super.onOptionsItemSelected(item)
            }
            R.id.shareItem -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, viewModel.getUriForSharing())
                intent.type = fileData?.contentType
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, "Share file"))
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateActionBarAndStatusBar(color: Int) {
        val window: Window? = activity?.window
        val supportActionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (color == Color.BLACK) {
            supportActionBar?.title = fileData?.displayName
            supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            window?.statusBarColor = Color.BLACK
        } else {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
            window?.statusBarColor = color
        }
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        updateActionBarAndStatusBar(Color.BLACK)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        updateActionBarAndStatusBar(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.wvFile.destroy()
    }
}