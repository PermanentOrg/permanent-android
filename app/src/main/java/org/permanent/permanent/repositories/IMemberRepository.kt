package org.permanent.permanent.repositories

import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface IMemberRepository {

    fun getMembers(listener: IDataListener)

    fun addMember(email: String, accessRole: AccessRole, listener: IResponseListener)

    fun updateMember(
        accountId: Int, email: String, accessRole: AccessRole, listener: IResponseListener
    )

    fun deleteMember(accountId: Int, email: String, listener: IResponseListener)
}