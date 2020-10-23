package org.permanent.permanent.ui.myFiles

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.permanent.Constants
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentAddOptionsBinding
import org.permanent.permanent.viewmodels.AddOptionsViewModel

class AddOptionsFragment: BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddOptionsBinding
    private lateinit var viewModel: AddOptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AddOptionsViewModel::class.java)
        binding.viewModel = viewModel
        binding.btnUpload.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(view: View) {
        // on upload btn click
        context?.let {
            val permissionHelper = PermissionsHelper()
            if (!permissionHelper.hasReadStoragePermission(it)) {
                permissionHelper.requestReadStoragePermission(this)
            } else {
                startFileSelectionActivity()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.REQUEST_CODE_READ_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    startFileSelectionActivity()
                } else {
                    Toast.makeText(
                        context, R.string.upload_no_permissions_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun startFileSelectionActivity() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_FILE_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            Constants.REQUEST_CODE_FILE_SELECT -> if (resultCode == Activity.RESULT_OK) {

                if (intent?.data != null) {
                    upload(intent.data!!)

                } else if (intent?.clipData != null) {
                    val clipData: ClipData = intent.clipData!!
                    val itemCount = clipData.itemCount
                    val uris: MutableList<Uri> = ArrayList()
                    var i = 0
                    while (i < itemCount) {
                        val originalUri = clipData.getItemAt(i).uri
                        uris.add(originalUri)
                        i++
                    }
                    upload(uris)
                }
            }
        }
    }

    private fun upload(originalUri: Uri) {
        requireActivity().contentResolver.takePersistableUriPermission(
            originalUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewModel.setupUploadWorker(originalUri)
    }

    private fun upload(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setupUploadWorkers(uris)
        }
    }
}