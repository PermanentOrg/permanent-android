package org.permanent.permanent.ui.public

import org.permanent.permanent.models.Archive

interface PublicArchiveListener {
    fun onArchiveClick(archive: Archive)
    fun onShareClick(archive: Archive)
}