package org.permanent.permanent.repositories

import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface IArchiveRepository {

    fun getArchivesByNr(archiveNrs: List<String?>, listener: IDataListener)

    fun searchArchive(name: String?, listener: IDataListener)

    fun updateProfilePhoto(thumbRecord: Record, listener: IResponseListener)

    fun getAllArchives(listener: IDataListener)

    fun acceptArchives(archives: List<Archive>, listener: IResponseListener)

    fun declineArchive(archive: Archive, listener: IResponseListener)

    fun switchToArchive(archiveNr: String, listener: IDataListener)

    fun createNewArchive(name: String, type: ArchiveType, listener: IArchiveListener)

    fun deleteArchive(archiveNr: String, listener: IResponseListener)

    fun getMembers(listener: IDataListener)

    fun addMember(email: String, accessRole: AccessRole, listener: IResponseListener)

    fun updateMember(
        accountId: Int, email: String, accessRole: AccessRole, listener: IResponseListener
    )

    fun transferOwnership(email: String, listener: IResponseListener)

    fun deleteMember(accountId: Int, email: String, listener: IResponseListener)

    interface IArchiveListener {
        fun onSuccess(archive: Archive)
        fun onFailed(error: String?)
    }
}