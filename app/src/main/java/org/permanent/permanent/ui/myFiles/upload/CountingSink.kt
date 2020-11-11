package org.permanent.permanent.ui.myFiles.upload

import okhttp3.RequestBody
import okio.Buffer
import okio.ForwardingSink
import okio.Sink

class CountingSink(
    sink: Sink,
    requestBody: RequestBody,
    private val listener: CountingRequestListener
) : ForwardingSink(sink) {
    private var bytesWritten = 0L
    private var reportedProgress = 0L
    private var updateInterval = 7
    private val contentLength = requestBody.contentLength()

    override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)
        bytesWritten += byteCount
        val newProgress = 100 * bytesWritten / contentLength
        if (newProgress >= reportedProgress + updateInterval) {
            reportedProgress = newProgress
            listener.onProgressUpdate(reportedProgress)
        }
    }
}