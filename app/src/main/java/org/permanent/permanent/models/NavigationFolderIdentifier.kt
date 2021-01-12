package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

class NavigationFolderIdentifier(val folderId: Int, val folderLinkId: Int) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<NavigationFolderIdentifier> {
        override fun createFromParcel(parcel: Parcel): NavigationFolderIdentifier {
            return NavigationFolderIdentifier(parcel)
        }

        override fun newArray(size: Int): Array<NavigationFolderIdentifier?> {
            return arrayOfNulls(size)
        }
    }
}