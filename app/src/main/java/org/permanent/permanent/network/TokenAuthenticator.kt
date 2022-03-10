package org.permanent.permanent.network

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.ui.showLoginScreen
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TokenAuthenticator : Authenticator {

    private var MAX_RETRIES = 5
    val authStateManager = AuthStateManager.getInstance(PermanentApplication.instance)
    val authState = authStateManager.current
    val authService = AuthorizationService(PermanentApplication.instance)

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        return if (responseCount(response) > MAX_RETRIES) {
            logout()
            null
        } else {
            if (response.code == 401) {
                FirebaseMessaging.getInstance().deleteToken();
                PermanentApplication.instance.showLoginScreen()
//                var accessToken: String?
//                runBlocking {
//                    accessToken = try {
//                        getRefreshedCredentialsAsync()
//                    } catch (exception: java.lang.Exception) {
//                        Log.e("TokenAuthenticator", "failed to refresh credentials")
//                        null
//                    }
//                }
//                // retry the failed 401 request with new access token
//                return response.request.newBuilder()
//                    .header(
//                        "Authorization",
//                        "Bearer $accessToken"
//                    ) // use the new access token
//                    .build()
            }
            return null
        }
    }

    private suspend fun getRefreshedCredentialsAsync(): String? {
        val clientAuth = ClientSecretBasic(BuildConfig.AUTH_CLIENT_SECRET)

        return suspendCancellableCoroutine { cont ->
            authState.performActionWithFreshTokens(authService, clientAuth
            ) { accessToken, idToken, exception ->
                if (exception != null) {
                    // negotiation for fresh tokens failed, check ex for more details
                    if (cont.isActive) {
                        cont.resumeWithException(exception)
                    }
                }
                if (cont.isActive) {
                    authStateManager.replace(authState)
                    cont.resume(accessToken)
                }
            }
        }
    }

    private fun logout() {
    }

    private fun responseCount(response: Response): Int {
        var res = response
        var result = 1
        while (res.priorResponse?.also { res = it } != null) {
            result++
        }
        return result
    }
}