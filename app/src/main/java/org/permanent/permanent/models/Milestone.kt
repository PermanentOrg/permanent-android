package org.permanent.permanent.models

class Milestone() {

    var id: Int? = null
    var title: String? = null
    var location: String? = null
    var startDate: String? = null
    var endDate: String? = null
    var description: String? = null

    constructor(profileItem: ProfileItem?) : this() {
        id = profileItem?.id
        title = profileItem?.string1
        location = profileItem?.locationVO?.getUIAddress()
        startDate = profileItem?.day1
        endDate = profileItem?.day2
        description = profileItem?.string2
    }
}