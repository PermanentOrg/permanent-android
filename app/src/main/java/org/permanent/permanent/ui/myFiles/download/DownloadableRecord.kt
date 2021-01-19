package org.permanent.permanent.ui.myFiles.download

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.ArchiveVO
import org.permanent.permanent.network.models.ItemVO

class DownloadableRecord(item: ItemVO, archive: ArchiveVO, showArchiveThumbnail: Boolean
): Record(item, archive, showArchiveThumbnail) {
    var isEnqueued = MutableLiveData(false)
    var isDownloading = MutableLiveData(false)
    var progress = MutableLiveData(0)

    fun observe(lifecycleOwner: LifecycleOwner, download: Download) {
        download.isEnqueued.observe(lifecycleOwner, {
            isEnqueued.value = it
        })
        download.isDownloading.observe(lifecycleOwner, {
            isDownloading.value = it
        })
        download.progress.observe(lifecycleOwner, {
            progress.value = it
        })
    }
}