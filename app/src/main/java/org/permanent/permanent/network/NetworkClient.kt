package org.permanent.permanent.network

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
import org.permanent.permanent.network.models.RequestContainer
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkClient {
    private val baseUrl: String = if (Constants.BUILD_ENV == BuildEnvOption.STAGING)
        Constants.URL_STAGING else Constants.URL_PROD
    private val retrofit: Retrofit
    private val loginService: LoginService
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val JSON: MediaType = "application/json;charset=UTF-8".toMediaType()

    init {
        val okHttpBuilder = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        okHttpBuilder.addInterceptor(interceptor)

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpBuilder.build())
            .build()

        loginService = retrofit.create(LoginService::class.java)
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
    }

    fun login(email: String, password: String): Call<ResponseVO> {
        val request: String = toJson(
            RequestContainer("").addAccount(email).addAccountPassword(password))
        val requestBody: RequestBody = request.toRequestBody(JSON)

        return retrofit.create(LoginService::class.java).login(requestBody)
    }

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }
}