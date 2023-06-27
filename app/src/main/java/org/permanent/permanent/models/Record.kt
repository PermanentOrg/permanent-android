package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.*

open class Record : Parcelable {
    var id: Int? = null
    var archiveNr: String? = null
    var archiveId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var folderLinkId: Int? = null
    var parentFolderArchiveNr: String? = null
    var parentFolderLinkId: Int? = null
    var displayName: String? = null
    var displayDate: String? = null
    var archiveFullName: String? = null
    var archiveThumbURL200: String? = null
    var showArchiveThumb: Boolean? = null
    var thumbURL200: String? = null
    var thumbURL2000: String? = null
    var isThumbBlurred: Boolean? = null
    var type: RecordType? = null
    var accessRole: AccessRole? = null
    var isRelocateMode: MutableLiveData<Boolean>? = null
    var isSelectMode: MutableLiveData<Boolean>? = null
    var isChecked: MutableLiveData<Boolean>? = null
    var shares: MutableList<Share>? = null
    var displayFirstInCarousel = false
    var isProcessing = false
    var displayInShares = false

    constructor(parcel: Parcel) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveNr = parcel.readString()
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolderArchiveNr = parcel.readString()
        parentFolderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDate = parcel.readString()
        archiveFullName = parcel.readString()
        archiveThumbURL200 = parcel.readString()
        showArchiveThumb = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        thumbURL200 = parcel.readString()
        thumbURL2000 = parcel.readString()
        isThumbBlurred = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
        shares = parcel.createTypedArrayList(Share)
        displayFirstInCarousel = parcel.readValue(Boolean::class.java.classLoader) as Boolean
        isProcessing = parcel.readValue(Boolean::class.java.classLoader) as Boolean
        displayInShares = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    constructor(recordInfo: RecordVO) {
        id = if (recordInfo.folderId != null) recordInfo.folderId else recordInfo.recordId
        archiveNr = recordInfo.archiveNbr
        archiveId = recordInfo.archiveId
        recordId = recordInfo.recordId
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        showArchiveThumb = false
        thumbURL200 = recordInfo.thumbURL200
        thumbURL2000 = recordInfo.thumbURL2000
        isThumbBlurred = false
        type = if (recordInfo.folderId != null) RecordType.FOLDER else RecordType.FILE
        accessRole = AccessRole.createFromBackendString(recordInfo.accessRole)
        initShares(recordInfo.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo.thumbURL200.isNullOrEmpty()
        displayInShares = false
    }

    constructor(recordInfo: FolderVO) {
        id = recordInfo.folderId
        archiveNr = recordInfo.archiveNbr
        archiveId = recordInfo.archiveId
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        showArchiveThumb = false
        thumbURL200 = recordInfo.thumbURL200
        thumbURL2000 = recordInfo.thumbURL2000
        isThumbBlurred = false
        type = RecordType.FOLDER
        accessRole = AccessRole.createFromBackendString(recordInfo.accessRole)
        initShares(recordInfo.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo.thumbURL200.isNullOrEmpty()
        displayInShares = false
    }

    constructor(itemVO: ItemVO, archiveVO: ArchiveVO, showArchiveThumbnail: Boolean) {
        id = if (itemVO.folderId != null) itemVO.folderId else itemVO.recordId
        archiveId = itemVO.archiveId
        archiveNr = itemVO.archiveNbr
        recordId = itemVO.recordId
        folderId = itemVO.folderId
        folderLinkId = itemVO.folder_linkId
        parentFolderLinkId = itemVO.parentFolder_linkId
        displayName = itemVO.displayName
        displayDate = itemVO.displayDT?.substringBefore("T")
        archiveFullName = "The ${archiveVO.fullName} Archive"
        archiveThumbURL200 = archiveVO.thumbURL200
        showArchiveThumb = showArchiveThumbnail
        thumbURL200 = itemVO.thumbURL200
        thumbURL2000 = itemVO.thumbURL2000
        isThumbBlurred = false
        type = if (itemVO.folderId != null) RecordType.FOLDER else RecordType.FILE
        accessRole = AccessRole.createFromBackendString(itemVO.accessRole)
        initShares(itemVO.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = itemVO.thumbURL200.isNullOrEmpty()
        displayInShares = true
    }

    constructor(recordId: Int, folderLinkId: Int) {
        id = recordId
        this.recordId = recordId
        this.folderLinkId = folderLinkId
        isThumbBlurred = false
        type = RecordType.FILE
        displayFirstInCarousel = false
        isProcessing = false
        displayInShares = false
    }

    constructor(archiveNr: String, folderLinkId: Int) {
        this.archiveNr = archiveNr
        this.folderLinkId = folderLinkId
        isThumbBlurred = false
        type = RecordType.FOLDER
        displayFirstInCarousel = false
        isProcessing = false
        displayInShares = false
    }

    constructor(shareByUrlVO: Shareby_urlVO) {
        val recordInfo = shareByUrlVO.RecordVO
        id = if (recordInfo?.folderId != null) recordInfo.folderId else recordInfo?.recordId
        archiveNr = recordInfo?.archiveNbr
        recordId = recordInfo?.recordId
        folderId = recordInfo?.folderId
        folderLinkId = recordInfo?.folder_linkId
        parentFolderLinkId = recordInfo?.parentFolder_linkId
        displayName = recordInfo?.displayName
        displayDate = recordInfo?.displayDT?.substringBefore("T")
        thumbURL200 = recordInfo?.thumbURL200
        thumbURL2000 = recordInfo?.thumbURL2000
        isThumbBlurred = shareByUrlVO.previewToggle == null || shareByUrlVO.previewToggle == 0
        type = if (recordInfo?.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo?.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo?.thumbURL200.isNullOrEmpty()
        displayInShares = false
    }

    private fun initShares(shareVOs: List<ShareVO>?) {
        shares = ArrayList()
        shareVOs?.let {
            for (shareVO in it) {
                (shares as ArrayList<Share>).add(Share(shareVO))
            }
        }
    }

    fun getFolderIdentifier(): NavigationFolderIdentifier? {
        if (folderId != null && folderLinkId != null)
            return NavigationFolderIdentifier(folderId!!, folderLinkId!!)
        return null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(archiveNr)
        parcel.writeValue(archiveId)
        parcel.writeValue(recordId)
        parcel.writeValue(folderId)
        parcel.writeValue(folderLinkId)
        parcel.writeString(parentFolderArchiveNr)
        parcel.writeValue(parentFolderLinkId)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
        parcel.writeString(archiveFullName)
        parcel.writeString(archiveThumbURL200)
        parcel.writeValue(showArchiveThumb)
        parcel.writeString(thumbURL200)
        parcel.writeString(thumbURL2000)
        parcel.writeValue(isThumbBlurred)
        parcel.writeParcelable(type, flags)
        parcel.writeParcelable(accessRole, flags)
        parcel.writeTypedList(shares)
        parcel.writeValue(displayFirstInCarousel)
        parcel.writeValue(isProcessing)
        parcel.writeValue(displayInShares)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Record> {
        override fun createFromParcel(parcel: Parcel): Record {
            return Record(parcel)
        }

        override fun newArray(size: Int): Array<Record?> {
            return arrayOfNulls(size)
        }
    }
}