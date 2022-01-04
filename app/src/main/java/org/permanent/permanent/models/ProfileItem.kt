package org.permanent.permanent.models

import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.network.models.Profile_itemVO

class ProfileItem() {

    var archiveNr: String? = null
    var id: Int? = null
    var type: ProfileItemType? = null
    var string1: String? = null
    var string2: String? = null
    var string3: String? = null
    var textData1: String? = null
    var day1: String? = null
    var day2: String? = null
    var locationText: String? = null

    constructor(profileItemVo: Profile_itemVO?) : this() {
        archiveNr = profileItemVo?.archiveNbr
        id = profileItemVo?.profile_itemId
        type = when (profileItemVo?.fieldNameUI) {
            ProfileItemType.BASIC.backendString -> ProfileItemType.BASIC
            ProfileItemType.GENDER.backendString -> ProfileItemType.GENDER
            ProfileItemType.SHORT_DESCRIPTION.backendString -> ProfileItemType.SHORT_DESCRIPTION
            ProfileItemType.DESCRIPTION.backendString -> ProfileItemType.DESCRIPTION
            ProfileItemType.BIRTH_INFO.backendString -> ProfileItemType.BIRTH_INFO
            ProfileItemType.SOCIAL_MEDIA.backendString -> ProfileItemType.SOCIAL_MEDIA
            ProfileItemType.MILESTONE.backendString -> ProfileItemType.MILESTONE
            else -> ProfileItemType.UNKNOWN
        }
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
}