package org.permanent.permanent.ui.myFiles.upload

import okhttp3.RequestBody
import okio.Buffer
import okio.ForwardingSink
import okio.Sink

class CountingSink(
    sink: Sink,
    private val requestBody: RequestBody,
    private val listener: CountingRequestListener
) : ForwardingSink(sink) {
    private var bytesWritten = 0L

    override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)

        bytesWritten += byteCount
        listener.onProgressUpdate(bytesWritten, requestBody.contentLength())
    }
}