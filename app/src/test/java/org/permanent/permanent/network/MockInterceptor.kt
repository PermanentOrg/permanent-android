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
import org.permanent.permanent.Constants
import java.io.IOException


class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (BuildConfig.DEBUG) {
            val request = chain.request()
            val uri = request.url.toUri().toString()
            val reqBodyString = bodyToString(request.body)
            val (code, responseString) = when {
                uri.endsWith(SIGNUP_SUFFIX)
                        && reqBodyString.contains(SUCCESSFUL_EMAIL) -> 200 to SIGNUP_SUCCESS_RESPONSE
                uri.endsWith(SIGNUP_SUFFIX)
                        && reqBodyString.contains(DUPLICATED_EMAIL) -> 400 to SIGNUP_DUPLICATE_RESPONSE
                uri.endsWith(LOGIN_SUFFIX)
                        && reqBodyString.contains(CORRECT_CREDENTIALS_EMAIL) -> 200 to LOGIN_SUCCESS_RESPONSE
                uri.endsWith(LOGIN_SUFFIX)
                        && reqBodyString.contains(INCORRECT_CREDENTIALS_EMAIL) -> 200 to LOGIN_FAILURE_RESPONSE
                else -> 404 to ""
            }

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(code)
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
        const val CORRECT_CREDENTIALS_EMAIL = "jane.doe@email.com"
        const val INCORRECT_CREDENTIALS_EMAIL = "incorrect@email.com"
        // Sign-up is a Request-Version 2 call: an AccountVO body on success, an error body otherwise.
        private const val SIGNUP_SUCCESS_RESPONSE =
            "{\"accountId\":1640,\"primaryEmail\":\"$SUCCESSFUL_EMAIL\",\"fullName\":\"John Doe\",\"defaultArchiveId\":1838}"
        private const val SIGNUP_DUPLICATE_RESPONSE = "{\"errors\":[\"${Constants.ERROR_EMAIL_DUPLICATED}\"]}"
        private const val LOGIN_SUCCESS_RESPONSE =
            "{\"Results\":[{\"data\":[{\"AccountVO\":{\"accountId\":1622,\"primaryEmail\":\"raluca_chis01@yahoo.com\",\"fullName\":\"Raluca Chis\",\"address\":\"\",\"address2\":null,\"country\":\"\",\"city\":\"\",\"state\":\"\",\"zip\":\"\",\"primaryPhone\":null,\"defaultArchiveId\":1820,\"level\":null,\"apiToken\":null,\"betaParticipant\":null,\"facebookAccountId\":null,\"googleAccountId\":null,\"status\":\"status.auth.ok\",\"type\":\"type.account.standard\",\"emailStatus\":\"status.auth.verified\",\"phoneStatus\":\"status.auth.none\",\"notificationPreferences\":\"{\\\"textPreference\\\": {\\\"apps\\\": {\\\"confirmations\\\": 1}, \\\"share\\\": {\\\"requests\\\": 1, \\\"activities\\\": 1, \\\"confirmations\\\": 1}, \\\"account\\\": {\\\"confirmations\\\": 1, \\\"recommendations\\\": 1}, \\\"archive\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}, \\\"relationships\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}}, \\\"emailPreference\\\": {\\\"apps\\\": {\\\"confirmations\\\": 1}, \\\"share\\\": {\\\"requests\\\": 1, \\\"activities\\\": 1, \\\"confirmations\\\": 1}, \\\"account\\\": {\\\"confirmations\\\": 1, \\\"recommendations\\\": 1}, \\\"archive\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}, \\\"relationships\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}}, \\\"inAppPreference\\\": {\\\"apps\\\": {\\\"confirmations\\\": 1}, \\\"share\\\": {\\\"requests\\\": 1, \\\"activities\\\": 1, \\\"confirmations\\\": 1}, \\\"account\\\": {\\\"confirmations\\\": 1, \\\"recommendations\\\": 1}, \\\"archive\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}, \\\"relationships\\\": {\\\"requests\\\": 1, \\\"confirmations\\\": 1}}}\",\"agreed\":null,\"optIn\":null,\"emailArray\":null,\"inviteCode\":null,\"rememberMe\":null,\"keepLoggedIn\":null,\"accessRole\":null,\"spaceTotal\":1073741824,\"spaceLeft\":1073461853,\"fileTotal\":null,\"fileLeft\":99998,\"changePrimaryEmail\":null,\"changePrimaryPhone\":null,\"createdDT\":\"2021-07-19T06:42:52\",\"updatedDT\":\"2021-08-04T14:16:28\"},\"ArchiveVO\":{\"ChildFolderVOs\":[],\"FolderSizeVOs\":[],\"RecordVOs\":[],\"accessRole\":\"access.role.owner\",\"fullName\":\"Raluca Chis\",\"spaceTotal\":null,\"spaceLeft\":null,\"fileTotal\":null,\"fileLeft\":null,\"relationType\":null,\"homeCity\":null,\"homeState\":null,\"homeCountry\":null,\"ItemVOs\":[],\"birthDay\":null,\"company\":null,\"description\":null,\"archiveId\":1820,\"publicDT\":\"2021-07-19T06:42:52\",\"archiveNbr\":\"00na-0000\",\"public\":null,\"view\":null,\"viewProperty\":null,\"thumbArchiveNbr\":null,\"imageRatio\":\"1.00\",\"type\":\"type.archive.person\",\"thumbStatus\":\"status.generic.ok\",\"thumbURL200\":\"https:\\/\\/stagingcdn.permanent.org\\/00na-0000.thumb.w200?t=1658212978&Expires=1658212978&Signature=NXRnMZcAMUaicOkTNRGJRfwUylAQM4YX1xgxjPaP8T2fWf-BBCKtvXvEnHPZlWLhRqbnCG7hIziafT~SLI8rcfwyY4qeu5nF2xNsA434egpUgYjtV5PHAJC1Gwt6Dyck4DUjAUyhZSlREitLxR~qmWfwN~cJ5vYoM9rAxsODhusbw~D-~AxOmYooq-rPRBjwebWYsSyGaF5lAp9assn5M1dwXYYFCFjoZ225Y31dsQfcZIXRaN1JR4ZFxCZAVqkYG7MK8Di9ZHwVD6kVi-auYu6YDsk-gxYZe-wge0SrHJNIGvKeGrq3JcPf-wlKLhoA16qH2gu5cmRYwuBtUFLX1Q__&Key-Pair-Id=APKAJP2D34UGZ6IG443Q\",\"thumbURL500\":\"https:\\/\\/stagingcdn.permanent.org\\/00na-0000.thumb.w500?t=1658212978&Expires=1658212978&Signature=j9IpeRITXu6Bi1tNUhIPOtDxpSpy2ElhKfyymGLSvJcHge~CYlqqxB-FX~Yn6cXMaj5vXCYX5iD18-ZR5QQniKtye8R57Bb6W-hDAkrRzwkkowVmX9h6derq3v3WRezzViHPcAa6lehgS8nFreHX~J0RVjOqwt7TblGxIv0tzbKnc1UsJgz1Jnh9OF6vWuaRACTFYZ9mSJ6NH6dAnrj5zifQo3Gd1YoOo4ND6fGq~of5Nba0QbxnLWsvY9ehpnnzoqAhQBAe6awkk~O3O3M~M7XSA2n3bOuaQL5CnWh3UHB~eDMV3o4JbYZ1VAwqE1DOafJ6CWFeszvXzWLAuVG43Q__&Key-Pair-Id=APKAJP2D34UGZ6IG443Q\",\"thumbURL1000\":\"https:\\/\\/stagingcdn.permanent.org\\/00na-0000.thumb.w1000?t=1658212978&Expires=1658212978&Signature=M1aij7p8ti~ic04kQYzr5G7rTtTG8NcppvU7-oILdUlZQ8n7RKJooY0FoGdg-A8awpL51Dmjfvv2cZs1n1zMAiOU6Mt7ot7IB5He-Ka-L9c8rJisnU2BDejNZ3ic5xgnwwexmIkaK3gYyL2uypI5NdntLnhmTkl2YimvzDF85f-DNitcxqcq~BQLI4QEdEfj8YDBh7X3XFb~EVsXHCZi9Up5uQs25IbTONx0Mk4whf8b~SMXUoIWy2gtErH1GpKLah8Ikw5aESjsLudQpD2khJvDR~yevh2tGB3jD1xbUyEfQbKfbS2nrRafqQHNEIGi3PE~fRGoa6~N6ZxFvqsZHQ__&Key-Pair-Id=APKAJP2D34UGZ6IG443Q\",\"thumbURL2000\":\"https:\\/\\/stagingcdn.permanent.org\\/00na-0000.thumb.w2000?t=1658212978&Expires=1658212978&Signature=A0FdwYYHIXOc~kCrYfBJHk-puWkNuidFrIK2jynMbebk63h~5FJ016w1eBwMkOx-R3tWeZede6dx6oI--g5N86tYt1sTbdU-Vj9IzJVwKM8zA7QSGaGON0vIA3NJRVwejwSf9CT-EWRH3QU4qjxC7LjRY-l4I9RV3eqhG2mZBp9U-dNTFYIN5MWXRoi877MFBqB0viN0~nD0SZunlahqsC0KqoHkHhuo3pQP4ikD1QVKxoao7LTKtxCnU84NtjU4lkg2zlOx1LOMzDXxTe54SGjyHEw6Kaxrfhlv~k~NO3IeJpVe1pMkkFfwixtfDaJoWiU94vQfpbyASWfJgwUpNQ__&Key-Pair-Id=APKAJP2D34UGZ6IG443Q\",\"thumbDT\":\"2022-07-19T06:42:58\",\"status\":\"status.generic.ok\",\"createdDT\":\"2021-07-19T06:42:52\",\"updatedDT\":\"2021-07-19T06:42:58\"}}],\"message\":[\"Successful Login.\"],\"status\":true,\"resultDT\":\"2021-08-08T19:01:33\",\"createdDT\":null,\"updatedDT\":null}],\"isSuccessful\":true,\"actionFailKeys\":[],\"isSystemUp\":true,\"systemMessage\":\"Everything is A-OK\",\"sessionId\":null,\"csrf\":\"7f65a7a7cca349e6a0984ea2665a539d\",\"createdDT\":null,\"updatedDT\":null}"
        private const val LOGIN_FAILURE_RESPONSE =
            "{\"Results\":[{\"data\":null,\"message\":[\"warning.signin.unknown\"],\"status\":false,\"resultDT\":\"2021-07-30T10:27:46\",\"createdDT\":null,\"updatedDT\":null}],\"isSuccessful\":false,\"actionFailKeys\":[0],\"isSystemUp\":true,\"systemMessage\":\"Everything is A-OK\",\"sessionId\":null,\"csrf\":\"bd08bba604eb47535df063674657f7d4\",\"createdDT\":null,\"updatedDT\":null}"
    }
}