package org.permanent.permanent.repositories


interface IAuthenticationRepository {
    fun verifyLoggedIn(listener: IOnLoggedInListener)
    fun login(email: String, password: String, listener: IOnLoginListener)
    fun logout(listener: IOnLogoutListener)
    fun forgotPassword(email: String, listener: IOnResetPasswordListener)
    fun sendSMSVerificationCode(listener: IOnSMSCodeSentListener)
    fun verifyCode(code: String, authType: String, listener: IOnVerifyListener)
    fun resetPassword(
        password: String, passwordConfirmation: String, listener: IOnResetPasswordListener
    )

    interface IOnLoggedInListener {
        fun onResponse(isLoggedIn: Boolean)
    }

    interface IOnLoginListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnLogoutListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnResetPasswordListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnVerifyListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnSMSCodeSentListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }
}