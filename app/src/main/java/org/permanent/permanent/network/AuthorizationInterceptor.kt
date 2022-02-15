package org.permanent.permanent.network


//class AuthorizationInterceptor : Interceptor {
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val authStateManager = AuthStateManager.getInstance(PermanentApplication.instance)
//        val authState = authStateManager.current
//        val authService = AuthorizationService(PermanentApplication.instance)
//
//        authState.performActionWithFreshTokens(authService,
//            AuthState.AuthStateAction { accessToken, idToken, exception ->
//                if (exception != null) {
//                    // negotiation for fresh tokens failed, check ex for more details
//                    return@AuthStateAction
//                }
//
//                // use the access token to do something ...
//                authStateManager.replace(authState)
//                val requestBuilder: Request.Builder = chain.request().newBuilder()
//                requestBuilder.header("Authorization", "Bearer $accessToken")
//                response = chain.proceed(requestBuilder.build())
//            })
//
//        return response
//    }
//}