package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class ProfileItemName(val backendString: String) : Parcelable {
    UNKNOWN("profile"),
    BASIC("profile.basic"),
    GENDER("profile.gender"),
    SHORT_DESCRIPTION("profile.blurb"),
    DESCRIPTION("profile.description"),
    BIRTH_INFO("profile.birth_info"),
    ESTABLISHED_INFO("profile.established_info"),
    SOCIAL_MEDIA("profile.social_media"),
    EMAIL("profile.email"),
    MILESTONE("profile.milestone"),
    TIMEZONE("profile.timezone");

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileItemName> {
        override fun createFromParcel(parcel: Parcel): ProfileItemName {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<ProfileItemName?> {
            return arrayOfNulls(size)
        }
    }
}