package org.permanent.permanent.repositories


interface ILoginRepository {
    fun verifyLoggedIn(listener: IOnLoggedInListener)
    fun login(email: String, password: String, listener: IOnLoginListener)
    fun forgotPassword(email: String, listener: IOnResetPasswordListener)
    fun verify(code: String, listener: IOnVerifyListener)

    interface IOnLoggedInListener {
        fun onResponse(isLoggedIn: Boolean)
    }

    interface IOnLoginListener {
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
}