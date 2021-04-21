package org.permanent.permanent.ui.fileView

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileViewBinding
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.FileViewViewModel
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL

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
        arguments?.getParcelable<Record>(PARCELABLE_RECORD_KEY)?.let {
            viewModel.setRecord(it)
        }
        binding.executePendingBindings()
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val onFileData = Observer<FileData> {
        fileData = it
        (activity as AppCompatActivity?)?.supportActionBar?.title = fileData?.displayName

        if (it != null && it.contentType?.contains(FileType.PDF.toString()) == true) {
            val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), it.fileName)

            if (file.exists()) {
                binding.pdfView.fromFile(file)
                    .enableSwipe(false)
                    .onError { error ->
                        error.message?.let { errorMsg ->
                            Log.e(FileViewFragment::class.java.simpleName, errorMsg) }
                    }
                    .enableAnnotationRendering(false)
                    .password(null)
                    .load()
            } else {
                Thread {
                    try {
                        val inputStream: InputStream = URL(it.fileURL).openStream()
                        activity?.runOnUiThread {
                            binding.pdfView.recycle()
                            binding.pdfView.fromStream(inputStream)
                                .enableSwipe(false)
                                .onError { error ->
                                    error.message?.let { errorMsg ->
                                        Log.e(FileViewFragment::class.java.simpleName, errorMsg) }
                                }
                                .enableAnnotationRendering(false)
                                .password(null)
                                .load()
                        }
                    } catch (e: IOException) {
                        e.message?.let { errorMsg ->
                            Log.e(FileViewFragment::class.java.simpleName, errorMsg)
                        }
                    }
                }.start()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.metadataItem -> {
                val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
                requireParentFragment().
                findNavController()
                    .navigate(R.id.action_filesContainerFragment_to_fileMetadataFragment, bundle)
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

    override fun connectViewModelEvents() {
        viewModel.getFileData().observe(this, onFileData)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getFileData().removeObserver(onFileData)
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.title = ""
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
    }
}