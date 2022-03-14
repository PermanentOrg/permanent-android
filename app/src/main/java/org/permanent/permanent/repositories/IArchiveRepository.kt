package org.permanent.permanent.repositories

import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface IArchiveRepository {

    fun updateProfilePhoto(thumbRecord: Record, listener: IResponseListener)

    fun getAllArchives(listener: IDataListener)

    fun acceptArchive(archive: Archive, listener: IResponseListener)

    fun declineArchive(archive: Archive, listener: IResponseListener)

    fun switchToArchive(archiveNr: String, listener: IDataListener)

    fun createNewArchive(name: String, type: ArchiveType, listener: IResponseListener)

    fun deleteArchive(archiveNr: String, listener: IResponseListener)

    fun getMembers(listener: IDataListener)

    fun addMember(email: String, accessRole: AccessRole, listener: IResponseListener)

    fun updateMember(
        accountId: Int, email: String, accessRole: AccessRole, listener: IResponseListener
    )

    fun transferOwnership(email: String, listener: IResponseListener)

    fun deleteMember(accountId: Int, email: String, listener: IResponseListener)
}