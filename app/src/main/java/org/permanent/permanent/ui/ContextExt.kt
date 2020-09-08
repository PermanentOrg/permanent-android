package org.permanent.permanent.ui

import android.content.Context
import java.io.IOException

@Throws(IOException::class)
fun Context.readJsonAsset(fileName: String): String {
    val inputStream = assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()

    return String(buffer, Charsets.UTF_8)
}