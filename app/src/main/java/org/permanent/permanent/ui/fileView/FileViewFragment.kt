package org.permanent.permanent.ui.fileView

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
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
class FileViewFragment : PermanentBaseFragment(), View.OnTouchListener, View.OnClickListener {

    private var supportActionBar: ActionBar? = null
    private lateinit var viewModel: FileViewViewModel
    private lateinit var binding: FragmentFileViewBinding
    private var fileData: FileData? = null
    private var downloadingAlert: AlertDialog? = null

    @SuppressLint("ClickableViewAccessibility")
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
        supportActionBar = (activity as AppCompatActivity?)?.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.webView.setOnTouchListener(this)
        binding.pdfView.setOnClickListener(this)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (supportActionBar?.isShowing == true) supportActionBar?.hide()
            else supportActionBar?.show()
        }
        return false
    }

    override fun onClick(v: View) {
        if (supportActionBar?.isShowing == true) supportActionBar?.hide()
        else supportActionBar?.show()
    }

    private val onShowMessage = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val onFileDownloaded = Observer<Void> { _ ->
        downloadingAlert?.cancel()
        viewModel.getUriForSharing()?.let { shareFile(it) }
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
                requireParentFragment().findNavController()
                    .navigate(R.id.action_filesContainerFragment_to_fileMetadataFragment, bundle)
                super.onOptionsItemSelected(item)
            }
            R.id.shareItem -> {
                val uriForSharing = viewModel.getUriForSharing()
                if (uriForSharing != null) {
                    shareFile(uriForSharing)
                } else {
                    downloadingAlert = context?.let {
                        AlertDialog.Builder(it)
                            .setTitle(getString(R.string.downloading_file_in_progress))
                            .setNegativeButton(getString(R.string.cancel_button)
                            ) { _, _ -> viewModel.cancelDownload() }
                            .create()
                    }
                    downloadingAlert?.show()

                    viewModel.downloadFile(this)
                }
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareFile(sharingUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, sharingUri)
        intent.type = fileData?.contentType
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share file"))
    }

    override fun connectViewModelEvents() {
        viewModel.getFileData().observe(this, onFileData)
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnFileDownloaded().observe(this, onFileDownloaded)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getFileData().removeObserver(onFileData)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnFileDownloaded().removeObserver(onFileDownloaded)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.title = ""
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
        disconnectViewModelEvents()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
    }
}