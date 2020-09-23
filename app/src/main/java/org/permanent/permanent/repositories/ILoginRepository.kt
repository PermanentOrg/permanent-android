package org.permanent.permanent.repositories


interface ILoginRepository {
    fun login(email: String, password: String, listener: IOnLoginListener)
    fun verify(code: String, listener: IOnVerifyListener)
    fun forgotPassword(email: String, listener: IOnResetPasswordListener)

    interface IOnLoginListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnVerifyListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnResetPasswordListener {
        fun onSuccess()
        fun onFailed(error: String?, errorCode: Int)
    }
}