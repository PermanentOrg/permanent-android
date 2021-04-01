package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.TagVO

data class Tag constructor(var tagId: String? = null,  var name: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this() {
        tagId = parcel.readString()
        name = parcel.readString()
    }

    constructor(tagVO: TagVO) : this() {
        tagId = tagVO.tagId
        name = tagVO.name
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tagId)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}