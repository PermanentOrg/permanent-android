package org.permanent.permanent.repositories

interface IAccountRepository {
    fun signUp(fullName:String, email: String, password: String, listener: IOnSignUpListener)
    fun update(phoneNumber: String, listener: IOnPhoneUpdatedListener)

    interface IOnSignUpListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnPhoneUpdatedListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }
}
