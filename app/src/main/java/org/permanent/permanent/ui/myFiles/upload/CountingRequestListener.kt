package org.permanent.permanent.ui.myFiles.upload

interface CountingRequestListener {
    fun onProgressUpdate(bytesWritten: Long, contentLength: Long)
}