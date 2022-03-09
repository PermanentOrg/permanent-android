package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

class Milestone() : Parcelable {
    var id: Int? = null
    var title: String? = null
    var location: String? = null
    var startDate: String? = null
    var endDate: String? = null
    var description: String? = null
    var isForPublicProfileScreen: Boolean = true

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        title = parcel.readString()
        location = parcel.readString()
        startDate = parcel.readString()
        endDate = parcel.readString()
        description = parcel.readString()
        isForPublicProfileScreen = parcel.readByte() != 0.toByte()
    }

    constructor(profileItem: ProfileItem?, isForPublicProfileScreen: Boolean) : this() {
        id = profileItem?.id
        title = profileItem?.string1
        location = profileItem?.locationVO?.getUIAddress()
        startDate = profileItem?.day1
        endDate = profileItem?.day2
        description = profileItem?.string2
        this.isForPublicProfileScreen = isForPublicProfileScreen
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(location)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(description)
        parcel.writeByte(if (isForPublicProfileScreen) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Milestone> {
        override fun createFromParcel(parcel: Parcel): Milestone {
            return Milestone(parcel)
        }

        override fun newArray(size: Int): Array<Milestone?> {
            return arrayOfNulls(size)
        }
    }
}