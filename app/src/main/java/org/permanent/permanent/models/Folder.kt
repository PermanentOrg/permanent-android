package org.permanent.permanent.models

import org.permanent.permanent.network.models.FolderVO

class Folder private constructor() {
    var records: List<Record>? = null

    constructor(folderVO: FolderVO) : this() {
        records = ArrayList()
        folderVO.ChildItemVOs?.let {
            for (recordVO in it) {
                val record = Record(recordVO)
                (records as ArrayList<Record>).add(record)
            }
        }
    }
}