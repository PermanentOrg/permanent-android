package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ShareVO

class Record private constructor() : Parcelable {
    var archiveNr: String? = null
    var archiveId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var folderLinkId: Int? = null
    var parentFolderLinkId: Int? = null
    var displayName: String? = null
    var displayDate: String? = null
    var thumbURL500: String? = null
    var type: RecordType? = null
    var isRelocateMode: MutableLiveData<Boolean>? = null
    var shares: MutableList<Share>? = null

    constructor(parcel: Parcel) : this() {
        archiveNr = parcel.readString()
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDate = parcel.readString()
        thumbURL500 = parcel.readString()
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        shares = parcel.createTypedArrayList(Share)
    }

    constructor(recordInfo: RecordVO) : this() {
        archiveNr = recordInfo.archiveNbr
        archiveId = recordInfo.archiveId
        recordId = recordInfo.recordId
        folderId = recordInfo.folderId
        folderLinkId = recordInfo.folder_linkId
        parentFolderLinkId = recordInfo.parentFolder_linkId
        displayName = recordInfo.displayName
        displayDate = recordInfo.displayDT?.substringBefore("T")
        thumbURL500 = recordInfo.thumbURL500
        initShares(recordInfo.ShareVOs)
    }

    private fun initShares(shareVOs: List<ShareVO>?) {
        shares = ArrayList()
        shareVOs?.let {
            for (shareVO in it) {
                val archiveVO = shareVO.ArchiveVO
                val share = Share()
                val archive = Archive()
                archive.fullName = "The " + archiveVO?.fullName + " Archive"
                archive.thumbURL200 = archiveVO?.fullName
                archive.thumbURL500 = archiveVO?.thumbURL500
                archive.thumbURL1000 = archiveVO?.thumbURL1000
                archive.thumbURL2000 = archiveVO?.thumbURL2000
                share.archive = archive
                (shares as ArrayList<Share>).add(share)
            }
        }
    }

    fun getFolderIdentifier(): FolderIdentifier? {
        if (folderId != null && folderLinkId != null)
            return FolderIdentifier(folderId!!, folderLinkId!!)
        return null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(archiveNr)
        parcel.writeValue(archiveId)
        parcel.writeValue(recordId)
        parcel.writeValue(folderId)
        parcel.writeValue(folderLinkId)
        parcel.writeValue(parentFolderLinkId)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
        parcel.writeString(thumbURL500)
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