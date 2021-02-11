package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.IBinder
import android.provider.OpenableColumns
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Context.hideKeyboardFrom(windowToken: IBinder) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

@SuppressLint("SimpleDateFormat")
fun Uri.getDisplayName(context: Context): String {
    var displayName = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
    val cursor = context.contentResolver.query(this, null, null,
        null, null)
    try {
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        nameIndex?.let {  displayName = cursor.getString(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return displayName
}

fun Uri.getFile(context: Context, displayName: String): File? {
    var output: OutputStream? = null
    try {
        val inputStream = context.contentResolver.openInputStream(this)
        if (inputStream != null) {
            val file = File(context.cacheDir, displayName)
            output = FileOutputStream(file)

            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
            return file
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        output?.close()
    }
    return null
}