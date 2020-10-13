package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.network.models.RecordVO

interface FileOptionsClickListener {
    fun onFileOptionsClick(file: RecordVO)
}