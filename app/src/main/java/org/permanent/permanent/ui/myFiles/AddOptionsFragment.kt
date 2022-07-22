package org.permanent.permanent.ui.myFiles

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_title_text_two_buttons.view.*
import org.permanent.permanent.Constants.Companion.FILE_PROVIDER_NAME
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_FILE_SELECT
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_IMAGE_CAPTURE
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_VIDEO_CAPTURE
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.REQUEST_CODE_READ_STORAGE_PERMISSION
import org.permanent.permanent.databinding.DialogCreateNewFolderBinding
import org.permanent.permanent.databinding.FragmentAddOptionsBinding
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.AddOptionsViewModel
import org.permanent.permanent.viewmodels.NewFolderViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val FOLDER_IDENTIFIER_KEY = "folder_identifier"
const val IS_SHOWN_IN_PUBLIC_FILES_KEY = "is_shown_in_public_files_key"

class AddOptionsFragment : PermanentBottomSheetFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAddOptionsBinding
    private lateinit var viewModel: AddOptionsViewModel
    private lateinit var dialogViewModel: NewFolderViewModel
    private lateinit var dialogBinding: DialogCreateNewFolderBinding
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri
    private var alertDialog: AlertDialog? = null
    private val filesToUpload = MutableLiveData<MutableList<Uri>>()
    private val onRefreshFolder = SingleLiveEvent<Void>()

    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onFolderCreated = Observer<Void> {
        onRefreshFolder.call()
        alertDialog?.dismiss()
        dismiss()
    }

    fun setBundleArguments(folderIdentifier: NavigationFolderIdentifier?, isShownInPublicFiles: Boolean) {
        val bundle = Bundle()
        bundle.putParcelable(FOLDER_IDENTIFIER_KEY, folderIdentifier)
        bundle.putBoolean(IS_SHOWN_IN_PUBLIC_FILES_KEY, isShownInPublicFiles)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[AddOptionsViewModel::class.java]
        binding.viewModel = viewModel
        dialogViewModel = ViewModelProvider(this)[NewFolderViewModel::class.java]
        binding.btnNewFolder.setOnClickListener(this)
        binding.btnTakePhoto.setOnClickListener(this)
        binding.btnTakeVideo.setOnClickListener(this)
        binding.btnUpload.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(view: View) {
        val permissionHelper = PermissionsHelper()
        when (view.id) {
            R.id.btnNewFolder -> showNewFolderDialog()
            R.id.btnTakePhoto -> context?.let {
                if (!permissionHelper.hasCameraPermission(it)) {
                    permissionHelper.requestCameraPermission(this)
                } else {
                    dispatchTakePictureIntent()
                }
            }
            R.id.btnTakeVideo -> context?.let {
                if (!permissionHelper.hasCameraPermission(it)) {
                    permissionHelper.requestCameraPermission(this)
                } else {
                    dispatchTakeVideoIntent()
                }
            }
            R.id.btnUpload -> context?.let {
                if (!permissionHelper.hasReadStoragePermission(it)) {
                    permissionHelper.requestReadStoragePermission(this)
                } else if (arguments?.getBoolean(IS_SHOWN_IN_PUBLIC_FILES_KEY) == true) {
                    showConfirmationDialog()
                } else {
                    startFileSelectionActivity()
                }
            }
        }
    }

    private fun showNewFolderDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_create_new_folder, null, false
        )
        dialogBinding.executePendingBindings()
        dialogBinding.lifecycleOwner = this
        dialogBinding.viewModel = dialogViewModel
        val thisContext = context

        if (thisContext != null) {
            alertDialog = AlertDialog.Builder(thisContext)
                .setView(dialogBinding.root)
                .create()
            dialogBinding.btnCreate.setOnClickListener {
                val currentFolderIdentifier =
                    arguments?.getParcelable<NavigationFolderIdentifier>(FOLDER_IDENTIFIER_KEY)
                dialogViewModel.createNewFolder(currentFolderIdentifier)
            }
            dialogBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    private fun showConfirmationDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_title_text_two_buttons, null)
        val alert = android.app.AlertDialog.Builder(context).setView(viewDialog).create()

        viewDialog.tvTitle.text = getString(R.string.dialog_public_files_upload_title)
        viewDialog.tvText.text = getString(R.string.dialog_public_files_upload_text)
        viewDialog.btnPositive.text = getString(R.string.button_upload)
        viewDialog.btnPositive.setOnClickListener {
            startFileSelectionActivity()
            alert.dismiss()
        }
        viewDialog.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.packageManager?.let {

                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                    null
                }
                photoFile?.let { file ->
                    context?.let { ctx ->
                        photoURI = FileProvider.getUriForFile(
                            ctx,
                            PermanentApplication.instance.packageName + FILE_PROVIDER_NAME,
                            file
                        )
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE)
                }
            }

        }
    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            context?.packageManager?.let {
                takeVideoIntent.resolveActivity(it)?.also {
                    startActivityForResult(takeVideoIntent, REQUEST_CODE_VIDEO_CAPTURE)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_READ_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (arguments?.getBoolean(IS_SHOWN_IN_PUBLIC_FILES_KEY) == true) {
                        showConfirmationDialog()
                    } else {
                        startFileSelectionActivity()
                    }
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
        startActivityForResult(intent, REQUEST_CODE_FILE_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_FILE_SELECT -> if (resultCode == RESULT_OK) {
                var urisToUpload = emptyList<Uri>().toMutableList()
                if (intent?.data != null) {
                    urisToUpload.add(intent.data!!)
                } else if (intent?.clipData != null) {
                    urisToUpload = getUris(intent)
                }
                // Requesting read uri permission
                for (uri in urisToUpload) {
                    context?.contentResolver?.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                // Start uploading files
                filesToUpload.value = urisToUpload
            }
            REQUEST_CODE_IMAGE_CAPTURE -> if (resultCode == RESULT_OK) {
                filesToUpload.value = mutableListOf(photoURI)
            }
            REQUEST_CODE_VIDEO_CAPTURE -> if (resultCode == RESULT_OK) {
                intent?.data?.let { filesToUpload.value = mutableListOf(it) }
            }
        }
        dismiss()
    }

    private fun getUris(intent: Intent): MutableList<Uri> {
        val clipData: ClipData = intent.clipData!!
        val itemCount = clipData.itemCount
        val uris: MutableList<Uri> = ArrayList()
        var i = 0
        while (i < itemCount) {
            val originalUri = clipData.getItemAt(i).uri
            uris.add(originalUri)
            i++
        }
        return uris
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getOnFilesSelected(): MutableLiveData<MutableList<Uri>> {
        return filesToUpload
    }

    fun getOnRefreshFolder(): MutableLiveData<Void> {
        return onRefreshFolder
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
