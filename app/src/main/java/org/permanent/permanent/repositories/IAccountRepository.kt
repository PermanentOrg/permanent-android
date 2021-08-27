package org.permanent.permanent.repositories

import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener

interface IAccountRepository {

    fun signUp(fullName:String, email: String, password: String, listener: IResponseListener)

    fun getAccount(listener: IAccountListener)

    fun update(account: Account, listener: IResponseListener)

    fun changeDefaultArchive(defaultArchiveId: Int, listener: IResponseListener)

    fun delete(listener: IResponseListener)

    fun changePassword(
        currentPassword: String, newPassword: String, retypedPassword: String,
        listener: IResponseListener
    )

    interface IAccountListener {
        fun onSuccess(account: Account)
        fun onFailed(error: String?)
    }
}
