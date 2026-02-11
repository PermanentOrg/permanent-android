package org.permanent.permanent.ui.fileView

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
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
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.recordMenu.RecordMenuFragment
import org.permanent.permanent.viewmodels.FileViewViewModel
import java.io.IOException
import java.io.InputStream
import java.net.URL

const val PARCELABLE_FILE_DATA_KEY = "parcelable_file_data_key"

class FileViewFragment : PermanentBaseFragment(), View.OnTouchListener, View.OnClickListener {

    private var supportActionBar: ActionBar? = null
    private lateinit var viewModel: FileViewViewModel
    private lateinit var binding: FragmentFileViewBinding
    private var record: Record? = null
    private var fileData: FileData? = null
    private var recordMenuFragment: RecordMenuFragment? = null
    private lateinit var prefsHelper: PreferencesHelper

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

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        record?.let {
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

    private val onFileData = Observer<FileData> {
        fileData = it
        (activity as AppCompatActivity?)?.supportActionBar?.title = fileData?.displayName

        if (it.contentType?.contains(FileType.PDF.toString()) == true) {

            Thread {
                try {
                    val inputStream: InputStream = URL(it.fileURL).openStream()
                    activity?.runOnUiThread {
                        binding.pdfView.recycle()
                        binding.pdfView.fromStream(inputStream)
                            .enableSwipe(false)
                            .onError { error ->
                                error.message?.let { errorMsg ->
                                    Log.e(FileViewFragment::class.java.simpleName, errorMsg)
                                }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.metadataItem -> {
                val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
                requireParentFragment().findNavController()
                    .navigate(R.id.action_filesContainerFragment_to_fileMetadataFragment, bundle)
                super.onOptionsItemSelected(item)
            }
            R.id.moreItem -> {
                showRecordMenuFragment()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRecordMenuFragment() {
        record?.let {

            val workspace = if (prefsHelper.getCurrentWorkspace() == Workspace.PUBLIC_FILES) Workspace.FILE_VIEW_PUBLIC_FILES
            else if (prefsHelper.getCurrentWorkspace() == Workspace.SHARES) Workspace.FILE_VIEW_SHARED_FILES
            else Workspace.FILE_VIEW_PRIVATE_FILES

            recordMenuFragment = RecordMenuFragment()
            recordMenuFragment?.setBundleArguments(it, workspace)
            recordMenuFragment?.show(parentFragmentManager, recordMenuFragment?.tag)
            recordMenuFragment?.getOnRecordPublishRequest()?.observe(this, onRecordPublishObserver)
            recordMenuFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadObserver)
        }
    }

    private val onRecordPublishObserver = Observer<Record> { record ->
        viewModel.publishRecord(record)
    }

    private val onFileDownloadObserver = Observer<Record> {
        viewModel.download(record = it, lifecycleOwner = this)
    }

    override fun connectViewModelEvents() {
        viewModel.getFileData().observe(this, onFileData)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getFileData().removeObserver(onFileData)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        recordMenuFragment?.getOnRecordPublishRequest()?.removeObserver(onRecordPublishObserver)
        recordMenuFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadObserver)
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