package org.permanent.permanent.network.models

import org.permanent.permanent.models.Record

interface IFolderChildrenListener {
    fun onSuccess(records: List<Record>)
    fun onFailed(error: String?)
}