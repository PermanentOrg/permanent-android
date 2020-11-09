package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

class FolderIdentifier(val folderId: Int, val folderLinkId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(folderId)
        parcel.writeInt(folderLinkId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FolderIdentifier> {
        override fun createFromParcel(parcel: Parcel): FolderIdentifier {
            return FolderIdentifier(parcel)
        }

        override fun newArray(size: Int): Array<FolderIdentifier?> {
            return arrayOfNulls(size)
        }
    }
}