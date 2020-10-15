package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.network.models.RecordVO

interface FileClickListener {
    fun onFileClick(file: RecordVO)
}