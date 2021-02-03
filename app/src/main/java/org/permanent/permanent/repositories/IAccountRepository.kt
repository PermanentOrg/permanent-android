package org.permanent.permanent.repositories

import org.permanent.permanent.network.IResponseListener

interface IAccountRepository {

    fun signUp(fullName:String, email: String, password: String, listener: IResponseListener)

    fun update(phoneNumber: String, listener: IResponseListener)

    fun changePassword(
        currentPassword: String, newPassword: String, retypedPassword: String,
        listener: IResponseListener
    )
}
