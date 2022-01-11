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
    var type: String? = "type.widget.string"
    var string1: String? = null
    var string2: String? = null
    var string3: String? = null
    var textData1: String? = null
    var day1: String? = null
    var day2: String? = null
    var locationText: String? = null

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
        locationText = parcel.readString()
    }

    constructor(profileItemVo: Profile_itemVO?) : this() {
        archiveNr = profileItemVo?.archiveNbr
        archiveId = profileItemVo?.archiveId
        id = profileItemVo?.profile_itemId
        fieldName = when (profileItemVo?.fieldNameUI) {
            ProfileItemName.BASIC.backendString -> ProfileItemName.BASIC
            ProfileItemName.GENDER.backendString -> ProfileItemName.GENDER
            ProfileItemName.SHORT_DESCRIPTION.backendString -> ProfileItemName.SHORT_DESCRIPTION
            ProfileItemName.DESCRIPTION.backendString -> ProfileItemName.DESCRIPTION
            ProfileItemName.BIRTH_INFO.backendString -> ProfileItemName.BIRTH_INFO
            ProfileItemName.SOCIAL_MEDIA.backendString -> ProfileItemName.SOCIAL_MEDIA
            ProfileItemName.MILESTONE.backendString -> ProfileItemName.MILESTONE
            else -> ProfileItemName.UNKNOWN
        }
        type = profileItemVo?.type
        string1 = profileItemVo?.string1
        string2 = profileItemVo?.string2
        string3 = profileItemVo?.string3
        textData1 = profileItemVo?.textData1
        day1 = profileItemVo?.day1
        day2 = profileItemVo?.day2
        profileItemVo?.LocnVOs?.let {
            val locationVO: LocnVO = it[0]
            val streetName =
                if (locationVO.streetName == null) "" else locationVO.streetName + ", "
            val addressValue =
                (locationVO.streetNumber ?: "") + " " + streetName + locationVO.locality +
                        ", " + locationVO.adminOneName + ", " + locationVO.countryCode
            if (!addressValue.contains("null")) locationText = addressValue.trim()
        }
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
        parcel.writeString(locationText)
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