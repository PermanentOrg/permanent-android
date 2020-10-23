package org.permanent.permanent

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionsHelper {

    fun hasSMSGroupPermission(ctx: Context): Boolean {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestSMSGroupPermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(
            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
            Constants.REQUEST_CODE_SMS_PERMISSION)
    }

    fun hasReadStoragePermission(ctx: Context): Boolean {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun requestReadStoragePermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            Constants.REQUEST_CODE_READ_STORAGE_PERMISSION)
    }
}
