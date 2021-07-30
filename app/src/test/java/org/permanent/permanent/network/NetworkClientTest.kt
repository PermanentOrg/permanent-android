package org.permanent.permanent.network

import android.content.Context
import com.google.common.truth.Truth
import okhttp3.OkHttpClient
import org.junit.Test

import org.mockito.Mockito

class NetworkClientTest {

    @Test
    fun signUpValidCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val networkClient = NetworkClient(
            okHttpClient,
            Mockito.mock(Context::class.java)
        )

        val responseVO = networkClient
            .signUp("name", MockInterceptor.SUCCESSFUL_EMAIL, "abcd1234")
            .execute()
            .body()

        val signupResponseSuccessful = responseVO?.isSuccessful != null && responseVO.isSuccessful!!

        Truth.assertThat(signupResponseSuccessful).isTrue()
    }

    @Test
    fun signUpInvalidCredentials() {
        val interceptor = MockInterceptor()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val networkClient = NetworkClient(
            okHttpClient,
            Mockito.mock(Context::class.java)
        )

        val responseVO = networkClient
            .signUp("name", MockInterceptor.DUPLICATED_EMAIL, "abcd1234")
            .execute()
            .body()

        val signupResponseSuccessful = responseVO?.isSuccessful != null && responseVO.isSuccessful!!

        Truth.assertThat(signupResponseSuccessful).isFalse()
    }
}