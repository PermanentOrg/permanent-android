package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ArchiveVO
import org.permanent.permanent.network.models.ItemVO

class ShareItem private constructor() : Parcelable {
    var archiveThumbUrl500: String? = null
    var type: RecordType? = null
    var itemThumbUrl500: String? = null
    var displayName: String? = null
    var displayDate: String? = null

    constructor(parcel: Parcel) : this() {
        archiveThumbUrl500 = parcel.readString()
        type = parcel.readParcelable(RecordType::class.java.classLoader)
        itemThumbUrl500 = parcel.readString()
        displayName = parcel.readString()
        displayDate = parcel.readString()
    }

    constructor(item: ItemVO, archive: ArchiveVO) : this() {
        type = if (item.folderId != null) RecordType.FOLDER else RecordType.FILE
        archiveThumbUrl500 = archive.thumbURL500
        itemThumbUrl500 = item.thumbURL500
        displayName = item.displayName
        displayDate = item.displayDT?.substringBefore("T")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(archiveThumbUrl500)
        parcel.writeParcelable(type, flags)
        parcel.writeString(itemThumbUrl500)
        parcel.writeString(displayName)
        parcel.writeString(displayDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShareItem> {
        override fun createFromParcel(parcel: Parcel): ShareItem {
            return ShareItem(parcel)
        }

        override fun newArray(size: Int): Array<ShareItem?> {
            return arrayOfNulls(size)
        }
    }
}