package org.permanent.permanent.repositories

interface ISignUpRepository {
    fun signUp(name:String?, email: String?, password: String?, listener: IOnSignUpListener?)

    interface IOnSignUpListener {
        fun onSuccess()
        fun onFailed(error: String?, errorCode: Int)
    }
}
