package org.permanent.permanent.models

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.myFiles.upload.UploadQueue

class Folder(val context: Context, private val folderInfo: RecordVO) {
    private var uploadQueue: UploadQueue? = null

    fun newUploadQueue(lifecycleOwner: LifecycleOwner, listener: Upload.IOnFinishedListener
    ): UploadQueue? {
        val archiveNr = folderInfo.archiveNbr
        val folderIdentifier = folderInfo.getFolderIdentifier()
        if (archiveNr != null && folderIdentifier != null) {
            uploadQueue =
                UploadQueue(context, folderIdentifier, lifecycleOwner, archiveNr, listener)
        }
        return uploadQueue
    }

    fun getUploadQueue() = uploadQueue

    fun getArchiveNr() = folderInfo.archiveNbr

    fun getDisplayName() = folderInfo.displayName

    fun getFolderIdentifier() = folderInfo.getFolderIdentifier()
}