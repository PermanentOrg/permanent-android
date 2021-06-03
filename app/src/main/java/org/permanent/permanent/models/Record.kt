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
    var parentFolderLinkId: Int? = null
    var displayName: String? = null
    var displayDate: String? = null
    var archiveThumbURL500: String? = null
    var showArchiveThumb: Boolean? = null
    var thumbURL500: String? = null
    var isThumbBlurred: Boolean? = null
    var type: RecordType? = null
    var isRelocateMode: MutableLiveData<Boolean>? = null
    var shares: MutableList<Share>? = null
    var status: String? = null
    var viewFirst = false
    var isProcessing = false

    constructor(parcel: Parcel) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveNr = parcel.readString()
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDate = parcel.readString()
        archiveThumbURL500 = parcel.readString()
        showArchiveThumb = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        thumbURL500 = parcel.readString()
        isThumbBlurred = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        shares = parcel.createTypedArrayList(Share)
        status = parcel.readString()
        viewFirst = parcel.readValue(Boolean::class.java.classLoader) as Boolean
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
        thumbURL500 = recordInfo.thumbURL500
        isThumbBlurred = true
        type = if (recordInfo.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo.ShareVOs)
        status = recordInfo.status
        viewFirst = false
        isProcessing = recordInfo.thumbURL500.isNullOrEmpty()
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
        archiveThumbURL500 = archive.thumbURL500
        showArchiveThumb = showArchiveThumbnail
        thumbURL500 = item.thumbURL500
        isThumbBlurred = true
        type = if (item.folderId != null) RecordType.FOLDER else RecordType.FILE
        status = item.status
        viewFirst = false
        isProcessing = item.thumbURL500.isNullOrEmpty()
    }

    constructor(recordId: Int, folderLinkId: Int) {
        id = recordId
        this.recordId = recordId
        this.folderLinkId = folderLinkId
        isThumbBlurred = true
        type = RecordType.FILE
        viewFirst = false
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
        thumbURL500 = recordInfo?.thumbURL500
        isThumbBlurred = shareByUrlVO.previewToggle == null || shareByUrlVO.previewToggle == 0
        type = if (recordInfo?.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo?.ShareVOs)
        status = recordInfo?.status
        viewFirst = false
        isProcessing = recordInfo?.thumbURL500.isNullOrEmpty()
    }

    private fun initShares(shareVOs: List<ShareVO>?) {
        shares = ArrayList()
        shareVOs?.let {
            for (shareVO in it) {
                val archiveVO = shareVO.ArchiveVO
                val share = Share(shareVO)
                share.archive?.fullName = "The " + archiveVO?.fullName + " Archive"
                (shares as ArrayList<Share>).add(share)
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
        parcel.writeValue(parentFolderLinkId)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
        parcel.writeString(archiveThumbURL500)
        parcel.writeValue(showArchiveThumb)
        parcel.writeString(thumbURL500)
        parcel.writeValue(isThumbBlurred)
        parcel.writeParcelable(type, flags)
        parcel.writeTypedList(shares)
        parcel.writeString(status)
        parcel.writeValue(viewFirst)
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