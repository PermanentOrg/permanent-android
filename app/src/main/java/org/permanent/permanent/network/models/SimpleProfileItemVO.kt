package org.permanent.permanent.network.models

import org.permanent.permanent.models.ProfileItem

class SimpleProfileItemVO(profileItem: ProfileItem) {
    var profile_itemId: Int? = null
    var publicDT: String? = null

    init {
        profile_itemId = profileItem.id
        publicDT = profileItem.publicDate
    }
}