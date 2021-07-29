package org.permanent.permanent.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.permanent.permanent.BuildConfig
import java.io.IOException


class MockInterceptor : Interceptor {

    private val responseMap = HashMap<String, String>()

    init {
        responseMap[SUCCESSFUL_EMAIL] = "{\"Results\":[{\"data\":[{\"AccountVO\":{\"accountId\":1640,\"primaryEmail\":\"flavia+prmnttst0001@server.com\",\"fullName\":\"Fla\",\"address\":null,\"address2\":null,\"country\":null,\"city\":null,\"state\":null,\"zip\":null,\"primaryPhone\":null,\"defaultArchiveId\":1838,\"level\":null,\"apiToken\":null,\"betaParticipant\":null,\"facebookAccountId\":null,\"googleAccountId\":null,\"status\":\"status.auth.ok\",\"type\":\"type.account.test\",\"emailStatus\":\"status.auth.unverified\",\"phoneStatus\":\"status.auth.none\",\"notificationPreferences\":{\"emailPreference\": {\"account\": {\"confirmations\": 1,\"recommendations\": 1},\"apps\": {\"confirmations\": 1},\"archive\": {\"confirmations\": 1,\"requests\": 1},\"relationships\": {\"confirmations\": 1,\"requests\": 1},\"share\": {\"activities\": 1,\"confirmations\": 1,\"requests\": 1}},\"inAppPreference\": {\"account\": {\"confirmations\": 1,\"recommendations\": 1},\"apps\": {\"confirmations\": 1},\"archive\": {\"confirmations\": 1,\"requests\": 1},\"relationships\": {\"confirmations\": 1,\"requests\": 1},\"share\": {\"activities\": 1,\"confirmations\": 1,\"requests\": 1}},\"textPreference\": {\"account\": {\"confirmations\": 1,\"recommendations\": 1},\"apps\": {\"confirmations\": 1},\"archive\": {\"confirmations\": 1,\"requests\": 1},\"relationships\": {\"confirmations\": 1,\"requests\": 1},\"share\": {\"activities\": 1,\"confirmations\": 1,\"requests\": 1}}},\"createdDT\":\"2021-07-28T16:07:34\",\"updatedDT\":\"2021-07-28T16:07:34\"}}],\"message\":[\"New account created accountId: 1640\"],\"status\":true,\"resultDT\":\"2021-07-28T16:07:36\",\"createdDT\":null,\"updatedDT\":null}],\"isSuccessful\":true,\"actionFailKeys\":[],\"isSystemUp\":true,\"systemMessage\":\"Everything is A-OK\",\"sessionId\":null,\"csrf\":\"91f1c9bc198212496d6004703858c6c7\",\"createdDT\":null,\"updatedDT\":null}"
        responseMap[DUPLICATED_EMAIL] = "{\"Results\":[{\"data\":null,\"message\":[\"warning.registration.duplicate_email\"],\"status\":false,\"resultDT\":\"2021-07-26T13:59:05\",\"createdDT\":null,\"updatedDT\":null}],\"isSuccessful\":false,\"actionFailKeys\":[0],\"isSystemUp\":true,\"systemMessage\":\"Everything is A-OK\",\"sessionId\":null,\"csrf\":\"ed88b0eb1557a7d5e7591683d154c625\",\"createdDT\":null,\"updatedDT\":null}"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (BuildConfig.DEBUG) {
            val request = chain.request()
            val uri = request.url.toUri().toString()
            val reqBodyString = bodyToString(request.body)
            val responseString: String = when {
                uri.endsWith(SIGNUP_SUFFIX)
                        && reqBodyString.contains(SUCCESSFUL_EMAIL) -> responseMap[SUCCESSFUL_EMAIL] ?: ""
                uri.endsWith(SIGNUP_SUFFIX)
                        && reqBodyString.contains(DUPLICATED_EMAIL) -> responseMap[DUPLICATED_EMAIL] ?: ""
                else -> ""
            }

            return chain.proceed(request)
                .newBuilder()
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("")
                .body(
                    responseString.toByteArray()
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
                .addHeader("content-type", "application/json")
                .build()
        } else {
            throw IllegalAccessError("MockInterceptor is only meant for Testing Purposes and " +
                    "bound to be used only with DEBUG mode")
        }
    }

    private fun bodyToString(request: RequestBody?): String {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            buffer.readUtf8()
        } catch (e: IOException) {
            Log.e(TAG, e.message.toString())
            ""
        }
    }

    companion object {
        private val TAG = MockInterceptor::class.java.simpleName
        private const val SIGNUP_SUFFIX = "account/post"
        private const val LOGIN_SUFFIX = "auth/login"
        const val SUCCESSFUL_EMAIL = "john.doe@email.com"
        const val DUPLICATED_EMAIL = "user@email.com"
    }
}