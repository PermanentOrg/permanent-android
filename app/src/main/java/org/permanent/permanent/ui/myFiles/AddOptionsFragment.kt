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
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.Constants
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogNewFolderBinding
import org.permanent.permanent.databinding.FragmentAddOptionsBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.AddOptionsViewModel
import org.permanent.permanent.viewmodels.NewFolderViewModel

class AddOptionsFragment: PermanentBottomSheetFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddOptionsBinding
    private lateinit var viewModel: AddOptionsViewModel
    private lateinit var dialogViewModel: NewFolderViewModel
    private lateinit var dialogBinding: DialogNewFolderBinding

    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onFolderCreated = Observer<Void> {
        super.dismiss()
    }

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
        dialogViewModel = ViewModelProvider(this).get(NewFolderViewModel::class.java)
        binding.btnNewFolder.setOnClickListener(this)
        binding.btnUpload.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btnNewFolder -> showNewFolderDialog()
            R.id.btnUpload -> context?.let {
                val permissionHelper = PermissionsHelper()
                if (!permissionHelper.hasReadStoragePermission(it)) {
                    permissionHelper.requestReadStoragePermission(this)
                } else {
                    startFileSelectionActivity()
                }
            }
        }
    }

    private fun showNewFolderDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_new_folder, null, false
        )
        dialogBinding.executePendingBindings()
        dialogBinding.lifecycleOwner = this
        dialogBinding.viewModel = dialogViewModel
        val thisContext = context

        if (thisContext != null) {
            val alert = AlertDialog.Builder(thisContext)
                .setView(dialogBinding.root)
                .create()
            dialogViewModel.setDialog(alert)
            alert.show()
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
                    dialogViewModel.errorStringId.value = R.string.upload_no_permissions_error
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
                    viewModel.upload(intent.data!!)

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
                    viewModel.upload(uris)
                }
            }
        }
    }

    override fun connectViewModelEvents() {
        dialogViewModel.getOnFolderCreated().observe(this, onFolderCreated)
        dialogViewModel.getErrorStringId().observe(this, onErrorStringId)
        dialogViewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        dialogViewModel.getOnFolderCreated().removeObserver(onFolderCreated)
        dialogViewModel.getErrorStringId().removeObserver(onErrorStringId)
        dialogViewModel.getErrorMessage().removeObserver(onErrorMessage)
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
