package org.permanent.permanent

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

const val REQUEST_CODE_READ_STORAGE_PERMISSION = 124
const val REQUEST_CODE_WRITE_STORAGE_PERMISSION = 125
const val REQUEST_CODE_CAMERA_PERMISSION = 126

class PermissionsHelper {

    fun hasReadStoragePermission(ctx: Context): Boolean {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestReadStoragePermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_READ_STORAGE_PERMISSION)
    }

    fun hasWriteStoragePermission(ctx: Context): Boolean {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestWriteStoragePermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_WRITE_STORAGE_PERMISSION)
    }

    fun hasCameraPermission(ctx: Context): Boolean {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestCameraPermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA_PERMISSION)
    }
}
