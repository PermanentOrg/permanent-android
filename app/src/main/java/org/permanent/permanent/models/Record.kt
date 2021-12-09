package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.*

open class Record : Parcelable {
    var id: Int? = null
    var archiveNr: String? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var folderLinkId: Int? = null
    var parentFolderArchiveNr: String? = null
    var parentFolderLinkId: Int? = null
    var displayName: String? = null
    var displayDate: String? = null
    var archiveThumbURL200: String? = null
    var showArchiveThumb: Boolean? = null
    var thumbURL200: String? = null
    var isThumbBlurred: Boolean? = null
    var type: RecordType? = null
    var isRelocateMode: MutableLiveData<Boolean>? = null
    var shares: MutableList<Share>? = null
    var displayFirstInCarousel = false
    var isProcessing = false

    constructor(parcel: Parcel) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveNr = parcel.readString()
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolderArchiveNr = parcel.readString()
        parentFolderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDate = parcel.readString()
        archiveThumbURL200 = parcel.readString()
        showArchiveThumb = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        thumbURL200 = parcel.readString()
        isThumbBlurred = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        shares = parcel.createTypedArrayList(Share)
        displayFirstInCarousel = parcel.readValue(Boolean::class.java.classLoader) as Boolean
        isProcessing = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    constructor(recordInfo: RecordVO) {
        id = if (recordInfo.folderId != null) recordInfo.folderId else recordInfo.recordId
        archiveNr = recordInfo.archiveNbr
        recordId = recordInfo.recordId
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        showArchiveThumb = false
        thumbURL200 = recordInfo.thumbURL200
        isThumbBlurred = false
        type = if (recordInfo.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo.thumbURL200.isNullOrEmpty()
    }

    constructor(recordInfo: FolderVO) {
        id = recordInfo.folderId
        archiveNr = recordInfo.archiveNbr
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        showArchiveThumb = false
        thumbURL200 = recordInfo.thumbURL200
        isThumbBlurred = false
        type = RecordType.FOLDER
        initShares(recordInfo.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo.thumbURL200.isNullOrEmpty()
    }

    constructor(item: ItemVO, archive: ArchiveVO, showArchiveThumbnail: Boolean) {
        id = if (item.folderId != null) item.folderId else item.recordId
        archiveNr = item.archiveNbr
        recordId = item.recordId
        folderId = item.folderId
        folderLinkId = item.folder_linkId
        parentFolderLinkId = item.parentFolder_linkId
        displayName = item.displayName
        displayDate = item.displayDT?.substringBefore("T")
        archiveThumbURL200 = archive.thumbURL200
        showArchiveThumb = showArchiveThumbnail
        thumbURL200 = item.thumbURL200
        isThumbBlurred = false
        type = if (item.folderId != null) RecordType.FOLDER else RecordType.FILE
        displayFirstInCarousel = false
        isProcessing = item.thumbURL200.isNullOrEmpty()
    }

    constructor(recordId: Int, folderLinkId: Int) {
        id = recordId
        this.recordId = recordId
        this.folderLinkId = folderLinkId
        isThumbBlurred = false
        type = RecordType.FILE
        displayFirstInCarousel = false
        isProcessing = false
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
        isThumbBlurred = shareByUrlVO.previewToggle == null || shareByUrlVO.previewToggle == 0
        type = if (recordInfo?.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo?.ShareVOs)
        displayFirstInCarousel = false
        isProcessing = recordInfo?.thumbURL200.isNullOrEmpty()
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
        parcel.writeValue(recordId)
        parcel.writeValue(folderId)
        parcel.writeValue(folderLinkId)
        parcel.writeString(parentFolderArchiveNr)
        parcel.writeValue(parentFolderLinkId)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
        parcel.writeString(archiveThumbURL200)
        parcel.writeValue(showArchiveThumb)
        parcel.writeString(thumbURL200)
        parcel.writeValue(isThumbBlurred)
        parcel.writeParcelable(type, flags)
        parcel.writeTypedList(shares)
        parcel.writeValue(displayFirstInCarousel)
        parcel.writeValue(isProcessing)
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