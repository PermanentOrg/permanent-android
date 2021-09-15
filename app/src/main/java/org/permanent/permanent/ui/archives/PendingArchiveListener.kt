package org.permanent.permanent.ui.archives

import org.permanent.permanent.models.Archive

interface PendingArchiveListener {
    fun onAcceptBtnClick(archive: Archive)
    fun onDeclineBtnClick(archive: Archive)
}