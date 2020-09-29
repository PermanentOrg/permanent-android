package org.permanent.permanent.repositories

interface ISignUpRepository {
    fun signUp(fullName:String, email: String, password: String, listener: IOnSignUpListener)

    interface IOnSignUpListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }
}
