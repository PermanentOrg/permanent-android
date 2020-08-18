package org.permanent.permanent.repositories


interface ILoginRepository {
    fun login(email: String?, password: String?, listener: IOnLoginListener?)
    fun forgotPassword(email: String?, listener: IOnResetPasswordListener?)

    interface IOnLoginListener {
        fun onSuccess()
        fun onFailed(error: String?, errorCode: Int)
    }

    interface IOnResetPasswordListener {
        fun onSuccess()
        fun onFailed(error: String?, errorCode: Int)
    }
}