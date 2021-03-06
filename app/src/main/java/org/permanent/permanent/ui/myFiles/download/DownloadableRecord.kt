package org.permanent.permanent.ui.myFiles.download

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.ArchiveVO
import org.permanent.permanent.network.models.ItemVO
import org.permanent.permanent.network.models.RecordVO

class DownloadableRecord : Record {
    lateinit var download: Download
    var isEnqueued = MutableLiveData(false)
    var isDownloading = MutableLiveData(false)
    var progress = MutableLiveData(0)

    constructor(item: ItemVO, archive: ArchiveVO, showArchiveThumbnail: Boolean)
            : super(item, archive, showArchiveThumbnail)

    constructor(recordInfo: RecordVO) : super(recordInfo)

    fun observe(theDownload: Download, lifecycleOwner: LifecycleOwner) {
        download = theDownload
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

    fun cancel() {
        download.cancel()
    }
}