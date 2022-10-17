package org.permanent.permanent.models

import android.content.Context
import android.net.Uri
import org.permanent.permanent.ui.getDisplayName
import org.permanent.permanent.ui.getMimeType
import org.permanent.permanent.ui.getSize

class File(val context: Context, val uri: Uri) {

    private var displayName: String = uri.getDisplayName(context)
    private var size: String = uri.getSize(context)
    private var mimeType: String? = uri.getMimeType(context)

    fun getDisplayName() = displayName

    fun getSize() = size
}