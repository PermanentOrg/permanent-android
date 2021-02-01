package org.permanent.permanent.network

import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.models.*
import org.permanent.permanent.network.models.*

const val PERM_API_KEY_MOBILE_STAGING = "0f6c8cf215a2a73a174ff45807a76be3"
const val PERM_API_KEY_MOBILE_PROD = "5aef7dd1f32e0d9ca57290e3c82b59db"

class RequestContainer(csrf: String?) {
    // Don't rename this property, is used in the api call
    private var RequestVO: RequestVO = RequestVO()

    init {
        if (Constants.BUILD_ENV === BuildEnvOption.STAGING) {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_STAGING
        } else {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_PROD
        }
        RequestVO.csrf = csrf
        val dataList = (RequestVO.data as ArrayList)
        dataList.add(Datum())
        RequestVO.data = dataList
    }

    fun addAccount(email: String): RequestContainer {
        val account = AccountVO()
        account.primaryEmail = email
        account.rememberMe = false
        RequestVO.data?.get(0)?.AccountVO = account
        return this
    }

    fun addAccount(accountId: Int, email: String, phoneNumber: String?): RequestContainer {
        val account = AccountVO()
        account.accountId = accountId
        account.primaryEmail = email
        account.primaryPhone = phoneNumber
        RequestVO.data?.get(0)?.AccountVO = account
        return this
    }

    fun addAccount(fullName: String, email: String): RequestContainer {
        val accountVO = AccountVO()
        accountVO.fullName = fullName
        accountVO.primaryEmail = email
        accountVO.optIn = false
        accountVO.agreed = true
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(email: String, accessRole: AccessRole): RequestContainer {
        val accountVO = AccountVO()
        accountVO.primaryEmail = email
        accountVO.accessRole = accessRole.toBackendString()
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(id: Int, email: String, accessRole: AccessRole): RequestContainer {
        addAccount(id, email)
        RequestVO.data?.get(0)?.AccountVO?.accessRole = accessRole.toBackendString()
        return this
    }

    fun addAccount(id: Int, email: String): RequestContainer {
        val accountVO = AccountVO()
        accountVO.accountId = id
        accountVO.primaryEmail = email
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccountPassword(password: String): RequestContainer {
        val accountPassword = AccountPasswordVO()
        accountPassword.password = password
        RequestVO.data?.get(0)?.AccountPasswordVO = accountPassword
        return this
    }

    fun addAccountPassword(password: String, passwordVerify: String): RequestContainer {
        addAccountPassword(password)
        RequestVO.data?.get(0)?.AccountPasswordVO?.passwordVerify = passwordVerify
        return this
    }

    fun addAuth(authToken: String, authType: String): RequestContainer {
        val auth = AuthVO()
        auth.token = authToken
        auth.type = authType
        RequestVO.data?.get(0)?.AuthVO = auth
        return this
    }

    fun addFolder(archiveNumber: String, sort: String?): RequestContainer {
        val folderVO = FolderVO()
        folderVO.archiveNbr = archiveNumber
        folderVO.sort = sort
        RequestVO.data?.get(0)?.FolderVO = folderVO
        return this
    }

    fun addFolder(archiveNumber: String, sort: String?, childLinks: List<Int?>): RequestContainer {
        addFolder(archiveNumber, sort)
        val childItems: MutableList<RecordVO> = ArrayList()
        for (childLink in childLinks) {
            val childFolder = RecordVO()
            childFolder.folder_linkId = childLink
            childItems.add(childFolder)
        }
        RequestVO.data?.get(0)?.FolderVO?.ChildItemVOs = childItems
        return this
    }

    fun addFolder(name: String, folderId: Int, folderLinkId: Int): RequestContainer {
        val folderVO = FolderVO()
        folderVO.displayName = name
        folderVO.parentFolderId = folderId
        folderVO.parentFolder_linkId = folderLinkId
        RequestVO.data?.get(0)?.FolderVO = folderVO
        return this
    }

    fun addRecord(
        displayName: String?,
        uploadName: String,
        parentFolderId: Int,
        parentFolderLinkId: Int
    ): RequestContainer {
        val recordVO = RecordVO()
        recordVO.displayName = displayName
        recordVO.uploadFileName = uploadName
        recordVO.isRecord = true
        recordVO.isFolder = false
        recordVO.isFetching = false
        recordVO.parentFolderId = parentFolderId
        recordVO.dataStatus = 0
        recordVO.parentFolder_linkId = parentFolderLinkId
        RequestVO.data?.get(0)?.RecordVO = recordVO
        return this
    }

    fun addRecord(
        folderLinkId: Int,
        archiveNr: String,
        archiveId: Int,
        recordId: Int
    ): RequestContainer {
        val recordVO = RecordVO()
        recordVO.archiveNbr = archiveNr
        recordVO.folder_linkId = folderLinkId
        recordVO.archiveId = archiveId
        recordVO.recordId = recordId
        recordVO.dataStatus = 1
        RequestVO.data?.get(0)?.RecordVO = recordVO
        return this
    }

    fun addRecord(record: Record): RequestContainer {
        return if (record.type == RecordType.FOLDER) {
            val folderVO = FolderVO()
            folderVO.folder_linkId = record.folderLinkId
            RequestVO.data?.get(0)?.FolderVO = folderVO
            this
        } else {
            val recordVO = RecordVO()
            recordVO.folder_linkId = record.folderLinkId
            recordVO.parentFolder_linkId = record.parentFolderLinkId
            RequestVO.data?.get(0)?.RecordVO = recordVO
            this
        }
    }

    fun addFolderDest(folderLinkId: Int): RequestContainer {
        val folderDestVO = FolderDestVO()
        folderDestVO.folder_linkId = folderLinkId
        RequestVO.data?.get(0)?.FolderDestVO = folderDestVO
        return this
    }

    fun addShareByUrl(shareByUrlVO: Shareby_urlVO): RequestContainer {
        RequestVO.data?.get(0)?.Shareby_urlVO = shareByUrlVO
        return this
    }

    fun addShareByUrl(urlToken: String): RequestContainer {
        val shareByUrlVO = Shareby_urlVO()
        shareByUrlVO.urlToken = urlToken
        RequestVO.data?.get(0)?.Shareby_urlVO = shareByUrlVO
        return this
    }

    fun addShare(share: Share): RequestContainer {
        val shareVO = ShareVO(share)
        shareVO.status = Status.OK.toBackendString()
        RequestVO.data?.get(0)?.ShareVO = shareVO
        return this
    }

    fun addArchive(archiveNr: String?): RequestContainer {
        val archiveVO = ArchiveVO()
        archiveVO.archiveNbr = archiveNr
        RequestVO.data?.get(0)?.ArchiveVO = archiveVO
        return this
    }

    fun addInvite(fullName: String, email: String): RequestContainer {
        val inviteVO = InviteVO()
        inviteVO.fullName = fullName
        inviteVO.email = email
        RequestVO.data?.get(0)?.InviteVO = inviteVO
        return this
    }

    fun addInvite(inviteId: Int): RequestContainer {
        val inviteVO = InviteVO()
        inviteVO.inviteId = inviteId
        RequestVO.data?.get(0)?.InviteVO = inviteVO
        return this
    }
}