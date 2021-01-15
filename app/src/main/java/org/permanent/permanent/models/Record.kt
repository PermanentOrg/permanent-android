package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.*

class Record private constructor() : Parcelable {
    var id: Int? = null
    var archiveNr: String? = null
    var archiveId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var folderLinkId: Int? = null
    var parentFolderLinkId: Int? = null
    var displayName: String? = null
    var displayDate: String? = null
    var archiveThumbURL500: String? = null
    var thumbURL500: String? = null
    var isThumbBlurred: Boolean? = null
    var type: RecordType? = null
    var isRelocateMode: MutableLiveData<Boolean>? = null
    var shares: MutableList<Share>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveNr = parcel.readString()
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDate = parcel.readString()
        archiveThumbURL500 = parcel.readString()
        thumbURL500 = parcel.readString()
        isThumbBlurred = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        shares = parcel.createTypedArrayList(Share)
    }

    constructor(recordInfo: RecordVO) : this() {
        id = if(recordInfo.folderId != null) recordInfo.folderId else recordInfo.recordId
        archiveNr = recordInfo.archiveNbr
        archiveId = recordInfo.archiveId
        recordId = recordInfo.recordId
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        thumbURL500 = recordInfo.thumbURL500
        type = if (recordInfo.folderId != null) RecordType.FOLDER else RecordType.FILE
        initShares(recordInfo.ShareVOs)
    }

    constructor(item: ItemVO, archive: ArchiveVO) : this() {
        id = if(item.folderId != null) item.folderId else item.recordId
        type = if (item.folderId != null) RecordType.FOLDER else RecordType.FILE
        archiveThumbURL500 = archive.thumbURL500
        thumbURL500 = item.thumbURL500
        displayName = item.displayName
        displayDate = item.displayDT?.substringBefore("T")
    }

    constructor(shareByUrlVO: Shareby_urlVO) : this() {
        val recordInfo = shareByUrlVO.RecordVO
        id = if(recordInfo?.folderId != null) recordInfo.folderId else recordInfo?.recordId
        archiveNr = recordInfo?.archiveNbr
        archiveId = recordInfo?.archiveId
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
        parcel.writeValue(archiveId)
        parcel.writeValue(recordId)
        parcel.writeValue(folderId)
        parcel.writeValue(folderLinkId)
        parcel.writeValue(parentFolderLinkId)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
        parcel.writeString(archiveThumbURL500)
        parcel.writeString(thumbURL500)
        parcel.writeValue(isThumbBlurred)
        parcel.writeParcelable(type, flags)
        parcel.writeTypedList(shares)
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