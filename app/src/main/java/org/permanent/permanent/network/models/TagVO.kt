package org.permanent.permanent.network.models

import org.permanent.permanent.models.Tag

class TagVO() {
    var tagId: String? = null
    var name: String? = null

    constructor(tag: Tag) : this() {
        tagId = tag.tagId
        name = tag.name
    }
}