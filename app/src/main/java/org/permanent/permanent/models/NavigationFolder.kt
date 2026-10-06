package org.permanent.permanent.models

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.upload.UploadQueue

class NavigationFolder(val context: Context, private val folderInfo: Record) {
    private var uploadQueue: UploadQueue? = null

    fun newUploadQueue(lifecycleOwner: LifecycleOwner, listener: OnFinishedListener
    ): UploadQueue? {
        val archiveNr = folderInfo.archiveNr
        val folderIdentifier = folderInfo.getFolderIdentifier()
        if (archiveNr != null && folderIdentifier != null) {
            uploadQueue =
                UploadQueue(context, lifecycleOwner, archiveNr, folderIdentifier, listener)
        }
        return uploadQueue
    }

    fun getUploadQueue() = uploadQueue

    fun getArchiveNr() = folderInfo.archiveNr

    fun getDisplayName() = folderInfo.displayName

    fun getFolderIdentifier() = folderInfo.getFolderIdentifier()

    fun getArchiveId() = folderInfo.archiveId

    fun getAccessRole() = folderInfo.accessRole

    fun getSavedSort() = folderInfo.savedSort

    fun setSavedSort(sortType: SortType) {
        folderInfo.savedSort = sortType
    }
}