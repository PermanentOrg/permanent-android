package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.IBinder
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.WindowMetricsCalculator
import net.openid.appauth.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


fun Context.hideKeyboardFrom(windowToken: IBinder) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            ).hideSoftInputFromWindow(windowToken, 0)
}

fun Context.showKeyboardFor(view: View) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            ).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Context.assetSize(resourceUri: Uri): Long {
    try {
        val descriptor = contentResolver.openAssetFileDescriptor(resourceUri, "r")
        val size = descriptor?.length ?: return 0
        descriptor.close()
        return size
    } catch (e: Resources.NotFoundException) {
        return 0
    }
}

fun Activity.computeWindowSizeClasses(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val width = metrics.bounds.width()
    val height = metrics.bounds.height()
    val density = resources.displayMetrics.density
    return WindowSizeClass.compute(width / density, height / density)
}

fun gbToBytes(gb: Int): Long = gb.toLong() * 1024 * 1024 * 1024

fun mbToBytes(mb: Int): Long = mb.toLong() * 1024 * 1024

fun bytesToHumanReadableString(bytes: Long): String {
    val unit = 1024.0
    if (bytes < unit)
        return "$bytes B"
    var result = bytes.toDouble()
    val unitsToUse = "KMGTPE"
    var i = 0
    val unitsCount = unitsToUse.length
    while (true) {
        result /= unit
        if (result < unit || i == unitsCount - 1)
            break
        ++i
    }
    return with(StringBuilder(9)) {
        append(String.format("%.2f ", result))
        append(unitsToUse[i])
        append("B")
    }.toString()
}

fun bytesToCustomHumanReadableString(bytes: Long, showDecimal: Boolean): String {
    val unit = 1024.0
    var result = bytes.toDouble()
    if (result < unit)
        return "$result MB"
    result /= unit
    val unitsToUse = "MGTPE"
    val unitsCount = unitsToUse.length
    val conversionLimit = 100
    var i = 0

    while (true) {
        result /= unit
        if (result < conversionLimit || i == unitsCount - 1)
            break
        ++i
    }

    val resultString = if (showDecimal) String.format("%.1f", result) else result.toInt().toString()

    return with(StringBuilder(9)) {
        append(resultString)
        append(" ")
        append(unitsToUse[i])
        append("B")
    }.toString()
}

@SuppressLint("SimpleDateFormat")
fun Uri.getDisplayName(context: Context): String {
    var displayName = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
    val cursor = context.contentResolver.query(this, null, null, null, null)
    try {
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        nameIndex?.let { displayName = cursor.getString(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return displayName
}

fun Uri.getSize(context: Context): String {
    var displaySize = -1L
    val cursor = context.contentResolver.query(
        this, null, null, null, null
    )
    try {
        val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
        cursor?.moveToFirst()
        sizeIndex?.let { displaySize = cursor.getLong(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return bytesToHumanReadableString(displaySize)
}

fun Uri.getMimeType(context: Context): String? {
    return context.contentResolver.getType(this)
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
