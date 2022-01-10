package org.permanent.permanent.network

import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType
import org.permanent.permanent.models.*
import org.permanent.permanent.network.models.*
import java.io.File


class RequestContainer(csrf: String?) {
    // Don't rename this property, is used in the api call
    private var RequestVO: RequestVO = RequestVO()

    init {
        RequestVO.csrf = csrf
        val dataList = (RequestVO.data as ArrayList)
        dataList.add(Datum())
        RequestVO.data = dataList
    }

    fun addAccount(account: Account): RequestContainer {
        val accountVO = AccountVO(account)
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(email: String): RequestContainer {
        val account = AccountVO()
        account.primaryEmail = email
        account.rememberMe = false
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
        accountVO.accessRole = accessRole.backendString
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(id: Int, email: String, accessRole: AccessRole): RequestContainer {
        addAccount(id, email)
        RequestVO.data?.get(0)?.AccountVO?.accessRole = accessRole.backendString
        return this
    }

    fun addAccount(id: Int, email: String): RequestContainer {
        val accountVO = AccountVO()
        accountVO.accountId = id
        accountVO.primaryEmail = email
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(id: Int): RequestContainer {
        val accountVO = AccountVO()
        accountVO.accountId = id
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccount(id: Int, email: String, defaultArchiveId: Int): RequestContainer {
        addAccount(id, email)
        RequestVO.data?.get(0)?.AccountVO?.defaultArchiveId = defaultArchiveId
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

    fun addAccountPassword(
        currentPassword: String, newPassword: String, retypedPassword: String
    ): RequestContainer {
        addAccountPassword(newPassword, retypedPassword)
        RequestVO.data?.get(0)?.AccountPasswordVO?.passwordOld = currentPassword
        return this
    }

    fun addAuth(authToken: String, authType: String): RequestContainer {
        val auth = AuthVO()
        auth.token = authToken
        auth.type = authType
        RequestVO.data?.get(0)?.AuthVO = auth
        return this
    }

    fun addFolder(archiveNr: String, folderLinkId: Int, sort: String?): RequestContainer {
        val folderVO = FolderVO()
        folderVO.archiveNbr = archiveNr
        folderVO.folder_linkId = folderLinkId
        folderVO.sort = sort
        RequestVO.data?.get(0)?.FolderVO = folderVO
        return this
    }

    fun addFolder(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        childLinks: List<Int?>
    ): RequestContainer {
        addFolder(archiveNr, folderLinkId, sort)
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

    fun addFolder(folderLinkId: Int): RequestContainer {
        val folderVO = FolderVO()
        folderVO.folder_linkId = folderLinkId
        RequestVO.data?.get(0)?.FolderVO = folderVO
        return this
    }

    fun addRecord(
        displayName: String?,
        file: File,
        parentFolderId: Int,
        parentFolderLinkId: Int
    ): RequestContainer {
        val recordVO = RecordVO()
        recordVO.displayName = displayName
        recordVO.uploadFileName = file.name
        recordVO.size = file.length()
        recordVO.parentFolderId = parentFolderId
        recordVO.parentFolder_linkId = parentFolderLinkId
        RequestVO.data?.get(0)?.RecordVO = recordVO
        return this
    }

    fun addRecord(
        folderLinkId: Int?,
        recordId: Int?
    ): RequestContainer {
        val recordVO = RecordVO()
        recordVO.folder_linkId = folderLinkId
        recordVO.recordId = recordId
        recordVO.dataStatus = 1
        RequestVO.data?.get(0)?.RecordVO = recordVO
        return this
    }

    fun addRecord(fileData: FileData): RequestContainer {
        val recordVO = RecordVO()
        recordVO.folder_linkId = fileData.folderLinkId
        recordVO.archiveNbr = fileData.archiveNr
        recordVO.recordId = fileData.recordId
        recordVO.displayName = fileData.displayName
        recordVO.description = fileData.description
        recordVO.displayDT = fileData.displayDate
        RequestVO.data?.get(0)?.RecordVO = recordVO
        return this
    }

    fun addRecord(locnVO: LocnVO, fileData: FileData): RequestContainer {
        val recordVO = RecordVO()
        recordVO.folder_linkId = fileData.folderLinkId
        recordVO.archiveNbr = fileData.archiveNr
        recordVO.recordId = fileData.recordId
        recordVO.LocnVO = locnVO
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

    fun addRecord(record: Record, newName: String): RequestContainer {
        return if (record.type == RecordType.FOLDER) {
            val folderVO = FolderVO()
            folderVO.folderId = record.id
            folderVO.folder_linkId = record.folderLinkId
            folderVO.displayName = newName
            RequestVO.data?.get(0)?.FolderVO = folderVO
            this
        } else {
            val recordVO = RecordVO()
            recordVO.recordId = record.id
            recordVO.displayName = newName
            RequestVO.data?.get(0)?.RecordVO = recordVO
            this
        }
    }

    fun addSearch(query: String?, tags: List<Tag>): RequestContainer {
        val searchVO = SearchVO()
        searchVO.query = query

        if (tags.isNotEmpty()) {
            val tagVOs = ArrayList<TagVO>()
            for (tag in tags) tagVOs.add(TagVO(tag))
            searchVO.TagVOs = tagVOs
        }

        searchVO.numberOfResults = 10
        RequestVO.data?.get(0)?.SearchVO = searchVO
        return this
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

    fun addArchive(archive: Archive): RequestContainer {
        val archiveVO = ArchiveVO(archive)
        archiveVO.status = Status.OK.toBackendString()
        RequestVO.data?.get(0)?.ArchiveVO = archiveVO
        return this
    }

    fun addArchive(archiveNr: String?): RequestContainer {
        val archiveVO = ArchiveVO()
        archiveVO.archiveNbr = archiveNr
        RequestVO.data?.get(0)?.ArchiveVO = archiveVO
        return this
    }

    fun addArchive(archiveId: Int): RequestContainer {
        val archiveVO = ArchiveVO()
        archiveVO.archiveId = archiveId
        RequestVO.data?.get(0)?.ArchiveVO = archiveVO
        return this
    }

    fun addArchive(name: String, type: ArchiveType): RequestContainer {
        val archiveVO = ArchiveVO()
        archiveVO.fullName = name
        archiveVO.type = type.backendString
        RequestVO.data?.get(0)?.ArchiveVO = archiveVO
        return this
    }

    fun addProfileItem(archiveNr: String?): RequestContainer {
        val profileItemVo = Profile_itemVO()
        profileItemVo.archiveNbr = archiveNr
        RequestVO.data?.get(0)?.Profile_itemVO = profileItemVo
        return this
    }

    fun addProfileItem(profileItem: ProfileItem): RequestContainer {
        val profileItemVO = Profile_itemVO(profileItem)
        RequestVO.data?.get(0)?.Profile_itemVO = profileItemVO
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

    fun addSimple(mediaType: MediaType): RequestContainer {
        val simpleVO = SimpleVO()
        simpleVO.key = "type"
        simpleVO.value = mediaType.type + "/" + mediaType.subtype
        RequestVO.data?.get(0)?.SimpleVO = simpleVO
        return this
    }

    fun addSimple(token: String): RequestContainer {
        val simpleVO = SimpleVO()
        simpleVO.key = "token"
        simpleVO.value = token
        RequestVO.data?.get(0)?.SimpleVO = simpleVO
        return this
    }

    fun addLocation(latLng: LatLng): RequestContainer {
        val locnVO = LocnVO()
        locnVO.latitude = latLng.latitude
        locnVO.longitude = latLng.longitude
        RequestVO.data?.get(0)?.LocnVO = locnVO
        return this
    }

    fun addTagNames(tags: List<Tag>): RequestContainer {
        for ((index, tag) in tags.withIndex()) {
            val tagVO = TagVO()
            tagVO.name = tag.name
            if (index == 0) RequestVO.data?.get(0)?.TagVO = tagVO
            else {
                val newData = Datum()
                newData.TagVO = tagVO
                (RequestVO.data as ArrayList).add(newData)
            }
        }
        return this
    }

    fun addTagIds(tags: List<Tag>): RequestContainer {
        for ((index, tag) in tags.withIndex()) {
            val tagVO = TagVO()
            tagVO.tagId = tag.tagId
            if (index == 0) RequestVO.data?.get(0)?.TagVO = tagVO
            else {
                val newData = Datum()
                newData.TagVO = tagVO
                (RequestVO.data as ArrayList).add(newData)
            }
        }
        return this
    }

    fun addTagLink(recordId: Int): RequestContainer {
        val dataList = RequestVO.data
        val tagLinkVO = TagLinkVO()
        tagLinkVO.refId = recordId

        if (dataList != null) {
            for (data in dataList) {
                data.TagLinkVO = tagLinkVO
            }
        }
        return this
    }
}