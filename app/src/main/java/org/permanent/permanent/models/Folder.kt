package org.permanent.permanent.models

import org.permanent.permanent.network.models.Shareby_urlVO

class Folder private constructor() {
    var records: List<Record>? = null

    constructor(shareByUrlVO: Shareby_urlVO) : this() {
        records = ArrayList()
        shareByUrlVO.FolderVO?.ChildItemVOs?.let {
            for (recordVO in it) {
                val record = Record(recordVO)
                record.isThumbBlurred =
                    shareByUrlVO.previewToggle == null || shareByUrlVO.previewToggle == 0
                (records as ArrayList<Record>).add(record)
            }
        }
    }
}