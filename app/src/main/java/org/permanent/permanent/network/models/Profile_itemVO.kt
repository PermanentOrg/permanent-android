package org.permanent.permanent.network.models

import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import java.util.*

class Profile_itemVO() {

    var archiveNbr: String? = null
    var archiveId: Int? = null
    var profile_itemId: Int? = null
    var fieldNameUI: String? = null
    var type: String? = null
    var string1: String? = null
    var string2: String? = null
    var string3: String? = null
    var textData1: String? = null
    var day1: String? = null
    var day2: String? = null
    var LocnVOs: List<LocnVO?>? = null
    var locnId1: Int? = null
    var publicDT: String? = null

    constructor(profileItem: ProfileItem) : this() {
        archiveNbr = profileItem.archiveNr
        archiveId = profileItem.archiveId
        profile_itemId = profileItem.id
        fieldNameUI = profileItem.fieldName?.backendString
        type = if (profileItem.type != null) profileItem.type
        else {
            if (profileItem.fieldName == ProfileItemName.MILESTONE) "type.widget.locn"
            else "type.widget.string"
        }
        string1 = profileItem.string1
        string2 = profileItem.string2
        string3 = profileItem.string3
        textData1 = profileItem.textData1
        day1 = profileItem.day1
        day2 = profileItem.day2
        profileItem.locationVO?.let {
            LocnVOs = ArrayList()
            (LocnVOs as ArrayList<LocnVO?>).add(it)
        }
        locnId1 = profileItem.locnId1
        publicDT = profileItem.publicDate
    }
}