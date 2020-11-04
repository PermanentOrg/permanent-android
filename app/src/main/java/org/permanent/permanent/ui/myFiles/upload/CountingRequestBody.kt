package org.permanent.permanent.ui.myFiles.upload

import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.buffer


class CountingRequestBody(private val requestBody: RequestBody,
                          private val listener: CountingRequestListener) : RequestBody() {

    override fun contentType() = requestBody.contentType()

    @Throws(IOException::class)
    override fun contentLength() = requestBody.contentLength()


    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink, this, listener)
        val bufferedSink = countingSink.buffer()

        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}