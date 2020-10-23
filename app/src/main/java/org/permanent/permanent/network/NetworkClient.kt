package org.permanent.permanent.network

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class NetworkClient(context: Context) {
    private val baseUrl: String = if (Constants.BUILD_ENV == BuildEnvOption.STAGING)
        Constants.URL_STAGING else Constants.URL_PROD
    private val retrofit: Retrofit
    private val authService: IAuthService
    private val accountService: IAccountService
    private val fileService: IFileService
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val jsonMediaType: MediaType = Constants.MEDIA_TYPE_JSON.toMediaType()
    private val uploadUrl = if (Constants.BUILD_ENV === BuildEnvOption.STAGING)
        Constants.URL_UPLOAD_STAGING else Constants.URL_UPLOAD_PROD

    init {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(),
            SharedPrefsCookiePersistor(context.applicationContext))

        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(interceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()

        authService = retrofit.create(IAuthService::class.java)
        accountService = retrofit.create(IAccountService::class.java)
        fileService = retrofit.create(IFileService::class.java)
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
    }

    fun verifyLoggedIn(): Call<ResponseVO> {
        val request = toJson(RequestContainer(""))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return authService.verifyLoggedIn(requestBody)
    }

    fun login(email: String, password: String): Call<ResponseVO> {
        val request: String = toJson(
            RequestContainer("").addAccount(email).addAccountPassword(password)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.login(requestBody)
    }

    fun logout(csrf: String?): Call<ResponseVO> {
        val request: String = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.logout(requestBody)
    }

    fun forgotPassword(email: String): Call<ResponseVO> {
        val request: String = toJson(RequestContainer("").addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.forgotPassword(requestBody)
    }

    fun sendSMSVerificationCode(csrf: String?, accountId: String, email: String): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf).addAccount(accountId, email, null))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.sendSMSVerificationCode(requestBody)
    }

    fun verifyCode(code: String, authType: String, csrf: String?, email: String): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf).addAuth(code, authType).addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.verifyCode(requestBody)
    }

    fun signUp(fullName: String, email: String, password: String): Call<ResponseVO> {
        val request = toJson(
            RequestContainer("")
                .addAccountPassword(password, password)
                .addAccount(fullName, email)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.signUp(requestBody)
    }

    fun update(csrf: String?, accountId: String, email: String, phoneNumber: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addAccount(accountId, email, phoneNumber))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.updatePhone(requestBody)
    }

    fun getRoot(csrf: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRoot(requestBody)
    }

    fun navigateMin(csrf: String?, archiveNumber: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addFolder(archiveNumber))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.navigateMin(requestBody)
    }

    fun getLeanItems(csrf: String?, archiveNumber: String, childItems: List<Int>): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addFolder(archiveNumber, childItems))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getLeanItems(requestBody)
    }

    fun createUploadMetaData(csrf: String?, fileName: String, displayName: String?, folderId: Int,
                             folderLinkId: Int): Call<ResponseVO> {
        val request =
            toJson(RequestContainer(csrf).addRecord(displayName, fileName, folderId, folderLinkId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.postMeta(requestBody)
    }

    fun uploadFile(file: File, mediaType: MediaType, recordId: Int): Call<ResponseBody> {
        val recordIdRequestBody = recordId.toString().toRequestBody(MultipartBody.FORM)
        val fileRequestBody = file.asRequestBody(mediaType)
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            Constants.FORM_DATA_NAME_THE_FILE, file.name, fileRequestBody)

        return fileService.upload(uploadUrl, recordIdRequestBody, body)
    }

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }
}