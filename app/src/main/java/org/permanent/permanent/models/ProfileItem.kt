package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.network.models.Profile_itemVO

class ProfileItem() : Parcelable {
    var archiveNr: String? = null
    var archiveId: Int? = null
    var id: Int? = null
    var fieldName: ProfileItemName? = null
    var type: String? = null
    var string1: String? = null
    var string2: String? = null
    var string3: String? = null
    var textData1: String? = null
    var day1: String? = null
    var day2: String? = null
    var locationVO: LocnVO? = null
    var locnId1: Int? = null
    var isForPublicProfileScreen: Boolean = true

    constructor(parcel: Parcel) : this() {
        archiveNr = parcel.readString()
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        fieldName = parcel.readParcelable(ProfileItemName::class.java.classLoader)
        type = parcel.readString()
        string1 = parcel.readString()
        string2 = parcel.readString()
        string3 = parcel.readString()
        textData1 = parcel.readString()
        day1 = parcel.readString()
        day2 = parcel.readString()
        locationVO = parcel.readParcelable(LocnVO::class.java.classLoader)
        locnId1 = parcel.readValue(Int::class.java.classLoader) as? Int
        isForPublicProfileScreen = parcel.readByte() != 0.toByte()
    }

    constructor(profileItemVO: Profile_itemVO?, isForPublicProfileScreen: Boolean) : this() {
        archiveNr = profileItemVO?.archiveNbr
        archiveId = profileItemVO?.archiveId
        id = profileItemVO?.profile_itemId
        fieldName = when (profileItemVO?.fieldNameUI) {
            ProfileItemName.BASIC.backendString -> ProfileItemName.BASIC
            ProfileItemName.GENDER.backendString -> ProfileItemName.GENDER
            ProfileItemName.SHORT_DESCRIPTION.backendString -> ProfileItemName.SHORT_DESCRIPTION
            ProfileItemName.DESCRIPTION.backendString -> ProfileItemName.DESCRIPTION
            ProfileItemName.BIRTH_INFO.backendString -> ProfileItemName.BIRTH_INFO
            ProfileItemName.ESTABLISHED_INFO.backendString -> ProfileItemName.ESTABLISHED_INFO
            ProfileItemName.SOCIAL_MEDIA.backendString -> ProfileItemName.SOCIAL_MEDIA
            ProfileItemName.EMAIL.backendString -> ProfileItemName.EMAIL
            ProfileItemName.MILESTONE.backendString -> ProfileItemName.MILESTONE
            else -> ProfileItemName.UNKNOWN
        }
        type = profileItemVO?.type
        string1 = profileItemVO?.string1
        string2 = profileItemVO?.string2
        string3 = profileItemVO?.string3
        textData1 = profileItemVO?.textData1
        day1 = profileItemVO?.day1
        day2 = profileItemVO?.day2
        profileItemVO?.LocnVOs?.let { locationVO = it[0] }
        locnId1 = profileItemVO?.locnId1
        this.isForPublicProfileScreen = isForPublicProfileScreen
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(archiveNr)
        parcel.writeValue(archiveId)
        parcel.writeValue(id)
        parcel.writeParcelable(fieldName, flags)
        parcel.writeString(type)
        parcel.writeString(string1)
        parcel.writeString(string2)
        parcel.writeString(string3)
        parcel.writeString(textData1)
        parcel.writeString(day1)
        parcel.writeString(day2)
        parcel.writeParcelable(locationVO, flags)
        parcel.writeValue(locnId1)
        parcel.writeByte(if (isForPublicProfileScreen) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileItem> {
        override fun createFromParcel(parcel: Parcel): ProfileItem {
            return ProfileItem(parcel)
        }

        override fun newArray(size: Int): Array<ProfileItem?> {
            return arrayOfNulls(size)
        }
    }
}