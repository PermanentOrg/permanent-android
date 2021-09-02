package org.permanent.permanent.repositories

import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface IArchiveRepository {

    fun getAllArchives(listener: IDataListener)

    fun switchToArchive(archiveNr: String?, listener: IResponseListener)

    fun createNewArchive(name: String, type: ArchiveType, listener: IResponseListener)

    fun deleteArchive(archiveNr: String, listener: IResponseListener)

    fun getMembers(listener: IDataListener)

    fun addMember(email: String, accessRole: AccessRole, listener: IResponseListener)

    fun updateMember(
        accountId: Int, email: String, accessRole: AccessRole, listener: IResponseListener
    )

    fun deleteMember(accountId: Int, email: String, listener: IResponseListener)
}