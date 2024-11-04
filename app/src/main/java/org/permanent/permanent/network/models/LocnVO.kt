package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable

class LocnVO() : Parcelable {
    var locnId: Int? = null
    var streetNumber: String? = null
    var streetName: String? = null
    var locality: String? = null
    var adminOneName: String? = null
    var countryCode: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    private var uiAddress: String? = null

    constructor(parcel: Parcel) : this() {
        locnId = parcel.readValue(Int::class.java.classLoader) as? Int
        streetNumber = parcel.readString()
        streetName = parcel.readString()
        locality = parcel.readString()
        adminOneName = parcel.readString()
        countryCode = parcel.readString()
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        uiAddress = parcel.readString()
    }

    fun getUIAddress(): String {
        if (uiAddress.isNullOrEmpty()) {
            val strName = if (streetName == null) "" else "$streetName, "
            val localityName = if (locality == null) "" else "$locality, "
            val adminName = if (adminOneName == null) "" else "$adminOneName, "
            val countryCodeName = if (countryCode == null) "" else "$countryCode"
            val addressValue = (streetNumber ?: "") + " " + strName +
                    localityName + adminName + countryCodeName
            uiAddress = if (!addressValue.contains("null")) addressValue.trim() else ""
        }

        return uiAddress!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(locnId)
        parcel.writeString(streetNumber)
        parcel.writeString(streetName)
        parcel.writeString(locality)
        parcel.writeString(adminOneName)
        parcel.writeString(countryCode)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(uiAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocnVO> {
        override fun createFromParcel(parcel: Parcel): LocnVO {
            return LocnVO(parcel)
        }

        override fun newArray(size: Int): Array<LocnVO?> {
            return arrayOfNulls(size)
        }
    }
}