package org.permanent.permanent.network

import android.content.Context
import com.google.common.truth.Truth
import okhttp3.OkHttpClient
import org.junit.Test
import org.permanent.permanent.Constants

import org.mockito.Mockito

class NetworkClientTest {

    @Test
    fun signUpValidCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val networkClient = NetworkClient(okHttpClient, Mockito.mock(Context::class.java))

        val response = networkClient
            .signUp("name", MockInterceptor.SUCCESSFUL_EMAIL, "abcd1234", false, "")
            .execute()

        Truth.assertThat(response.body()?.accountId).isEqualTo(1640)
    }

    @Test
    fun signUpInvalidCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val networkClient = NetworkClient(okHttpClient, Mockito.mock(Context::class.java))

        val response = networkClient
            .signUp("name", MockInterceptor.DUPLICATED_EMAIL, "abcd1234", false, "")
            .execute()

        Truth.assertThat(response.body()).isNull()
        Truth.assertThat(response.errorBody()?.string()).contains(Constants.ERROR_EMAIL_DUPLICATED)
    }

    @Test
    fun loginCorrectCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val networkClient = NetworkClient(okHttpClient, Mockito.mock(Context::class.java))

        val responseVO = networkClient
            .login(MockInterceptor.CORRECT_CREDENTIALS_EMAIL, "abcd1234")
            .execute()
            .body()

        val loginResponseSuccessful = responseVO?.isSuccessful != null && responseVO.isSuccessful!!

        Truth.assertThat(loginResponseSuccessful).isTrue()
    }

    @Test
    fun loginIncorrectCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val networkClient = NetworkClient(okHttpClient, Mockito.mock(Context::class.java))

        val responseVO = networkClient
            .login(MockInterceptor.INCORRECT_CREDENTIALS_EMAIL, "abcd1234")
            .execute()
            .body()

        val loginResponseSuccessful = responseVO?.isSuccessful != null && responseVO.isSuccessful!!

        Truth.assertThat(loginResponseSuccessful).isFalse()
    }
}