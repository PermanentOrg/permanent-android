package org.permanent.permanent.network.models

class SearchVO {
    var TagVOs: List<TagVO>? = null
    var query: String? = null
    var numberOfResults: Int? = null
    var ChildItemVOs: List<RecordVO>? = null
}