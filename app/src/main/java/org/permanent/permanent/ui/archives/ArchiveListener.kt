package org.permanent.permanent.ui.archives

import org.permanent.permanent.models.Archive

interface ArchiveListener {
    fun onArchiveClick(archive: Archive)
}