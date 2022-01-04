package org.permanent.permanent.network.models

import org.permanent.permanent.models.ProfileItem

class Profile_itemVO() {

    var archiveNbr: String? = null
    var profile_itemId: Int? = null
    var fieldNameUI: String? = null
    var string1: String? = null
    var string2: String? = null
    var string3: String? = null
    var textData1: String? = null
    var day1: String? = null
    var day2: String? = null
    var LocnVOs: List<LocnVO>? = null

    constructor(profileItem: ProfileItem) : this() {
        archiveNbr = profileItem.archiveNr
        profile_itemId = profileItem.id
        fieldNameUI = profileItem.type?.backendString
        string1 = profileItem.string1
        string2 = profileItem.string2
        string3 = profileItem.string3
        textData1 = profileItem.textData1
        day1 = profileItem.day1
        day2 = profileItem.day2
    }
}