package org.permanent.permanent.network

import android.app.Application
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkClient(application: Application) {
    private val baseUrl: String =
        if (Constants.BUILD_ENV == BuildEnvOption.STAGING) Constants.URL_STAGING
        else Constants.URL_PROD
    private val retrofit: Retrofit
    private val authService: AuthService
    private val accountService: AccountService
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val JSON: MediaType = "application/json;charset=UTF-8".toMediaType()

    init {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(
                SetCookieCache(),
                SharedPrefsCookiePersistor(application.applicationContext)
            )

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

        authService = retrofit.create(AuthService::class.java)
        accountService = retrofit.create(AccountService::class.java)
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
    }

    fun verifyLoggedIn(): Call<ResponseVO> {
        val request = toJson(RequestContainer(""))
        val requestBody: RequestBody = request.toRequestBody(JSON)
        return authService.verifyLoggedIn(requestBody)
    }

    fun login(email: String, password: String): Call<ResponseVO> {
        val request: String = toJson(
            RequestContainer("").addAccount(email).addAccountPassword(password)
        )
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return authService.login(requestBody)
    }

    fun forgotPassword(email: String): Call<ResponseVO> {
        val request: String = toJson(RequestContainer("").addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return authService.forgotPassword(requestBody)
    }

    fun verifyCode(code: String, csrf: String?, email: String): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf).addAuth(code).addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return authService.verifyCode(requestBody)
    }

    fun signUp(fullName: String, email: String, password: String): Call<ResponseVO> {
        val request = toJson(RequestContainer("")
            .addAccountPassword(password, password)
            .addAccount(fullName, email)
        )
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return accountService.signUp(requestBody)
    }

    fun update(csrf: String?, accountId: String, email: String, phoneNumber: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addAccount(accountId, email, phoneNumber))
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return accountService.updatePhone(requestBody)
    }

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }
}