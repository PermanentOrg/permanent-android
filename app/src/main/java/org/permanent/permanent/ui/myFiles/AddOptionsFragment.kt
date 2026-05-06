package org.permanent.permanent.ui.myFiles

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.Constants.Companion.FILE_PROVIDER_NAME
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_IMAGE_CAPTURE
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_PHOTO_LIBRARY_SELECT
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_VIDEO_CAPTURE
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.compose.AddOptionsScreen
import org.permanent.permanent.viewmodels.SingleLiveEvent
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val FOLDER_IDENTIFIER_KEY = "folder_identifier"
const val IS_SHOWN_IN_PUBLIC_FILES_KEY = "is_shown_in_public_files_key"

class AddOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri
    private var nameInputFragment: NameInputFragment? = null
    private val filesToUpload = MutableLiveData<MutableList<Uri>>()
    private val onRefreshFolder = SingleLiveEvent<Void?>()

    fun setBundleArguments(
        folderIdentifier: NavigationFolderIdentifier?, isShownInPublicFiles: Boolean
    ) {
        val bundle = Bundle()
        bundle.putParcelable(FOLDER_IDENTIFIER_KEY, folderIdentifier)
        bundle.putBoolean(IS_SHOWN_IN_PUBLIC_FILES_KEY, isShownInPublicFiles)
        this.arguments = bundle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                it.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(it)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme {
                AddOptionsScreen(
                    onNewFolder = { showNewFolderDialog() },
                    onTakePhoto = { handleCameraAction { dispatchTakePictureIntent() } },
                    onTakeVideo = { handleCameraAction { dispatchTakeVideoIntent() } },
                    onUploadPhotos = { handlePhotoLibraryUpload() }
                )
            }
        }
    }

    private fun showNewFolderDialog() {
        val folderIdentifier = arguments?.getParcelable<NavigationFolderIdentifier>(FOLDER_IDENTIFIER_KEY)
        nameInputFragment = NameInputFragment.forNewFolder(folderIdentifier)
        nameInputFragment?.setOnCompletedCallback {
            onRefreshFolder.call()
            dismiss()
        }
        nameInputFragment?.show(parentFragmentManager, nameInputFragment?.tag)
    }

    private fun handleCameraAction(action: () -> Unit) {
        val permissionHelper = DevicePermissionsHelper()
        context?.let {
            if (!permissionHelper.hasCameraPermission(it)) {
                permissionHelper.requestCameraPermission(this)
            } else {
                action()
            }
        }
    }

    private fun handlePhotoLibraryUpload() {
        if (arguments?.getBoolean(IS_SHOWN_IN_PUBLIC_FILES_KEY) == true) {
            showPublicFilesConfirmationDialog { startPhotoLibraryActivity() }
        } else {
            startPhotoLibraryActivity()
        }
    }

    private fun showPublicFilesConfirmationDialog(onConfirmed: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_public_files_upload_title))
            .setMessage(getString(R.string.dialog_public_files_upload_text))
            .setPositiveButton(getString(R.string.button_upload)) { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            context?.packageManager?.let {
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

    private fun startPhotoLibraryActivity() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_PHOTO_LIBRARY_SELECT)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_PHOTO_LIBRARY_SELECT -> if (resultCode == RESULT_OK) {
                var urisToUpload = emptyList<Uri>().toMutableList()
                if (intent?.data != null) {
                    urisToUpload.add(intent.data!!)
                } else if (intent?.clipData != null) {
                    urisToUpload = getUris(intent)
                }
                for (uri in urisToUpload) {
                    context?.contentResolver?.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
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
        val uris: MutableList<Uri> = ArrayList()
        for (i in 0 until clipData.itemCount) {
            uris.add(clipData.getItemAt(i).uri)
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

    fun getOnFilesSelected(): MutableLiveData<MutableList<Uri>> = filesToUpload

    fun getOnRefreshFolder(): MutableLiveData<Void?> = onRefreshFolder

    override fun connectViewModelEvents() {}

    override fun disconnectViewModelEvents() {}

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}
