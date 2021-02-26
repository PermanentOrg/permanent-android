package org.permanent.permanent.ui.myFiles

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.MediaController
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
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
        }
        binding.executePendingBindings()
        updateActionBarAndStatusBar(Color.BLACK)

        if (fileData?.contentType?.contains("video") == true) {
            // Set up the media controller widget and attach it to the video view.
            val controller = MediaController(context)
            controller.setMediaPlayer(binding.videoView)
            binding.videoView.setMediaController(controller)
        }
        return binding.root
    }

    private fun updateActionBarAndStatusBar(color: Int) {
        val window: Window? = activity?.window
        if (color == Color.BLACK) {
            supportActionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
            supportActionBar.title = fileData?.displayName
            supportActionBar.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            window?.statusBarColor = Color.BLACK
        } else {
            supportActionBar.setBackgroundDrawable(ColorDrawable(color))
            window?.statusBarColor = color
        }
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

    override fun onStart() {
        super.onStart()
        if (viewModel.showingVideo.value == true) {
            viewModel.isBusy.value = true
            binding.videoView.setVideoURI(viewModel.getVideoUri())
            binding.videoView.setOnPreparedListener {
                viewModel.isBusy.value = false
                binding.videoView.start()
            }
            binding.videoView.setOnInfoListener(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.videoView.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        updateActionBarAndStatusBar(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        binding.videoView.stopPlayback()
    }
}