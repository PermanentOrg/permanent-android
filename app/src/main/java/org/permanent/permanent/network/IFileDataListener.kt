package org.permanent.permanent.network

import org.permanent.permanent.network.models.FileData

interface IFileDataListener {
    fun onSuccess(fileData: FileData)
    fun onFailed(error: String?)
}
