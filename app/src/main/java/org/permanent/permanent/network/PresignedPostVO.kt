package org.permanent.permanent.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PresignedPostVO {
    var url: String? = null
    var fields: Map<String, String>? = null

    fun getFieldsMapForCall(): Map<String, RequestBody> {
        val map = HashMap<String, RequestBody>()
        val keys = fields?.keys

        if (keys != null) {
            for (key in keys) {
                fields?.get(key)?.toRequestBody(MultipartBody.FORM)?.let { map.put(key, it) }
            }
        }

        return map
    }
}