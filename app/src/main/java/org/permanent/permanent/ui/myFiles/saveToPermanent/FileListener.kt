package org.permanent.permanent.ui.myFiles.saveToPermanent

import org.permanent.permanent.models.File

interface FileListener {
    fun onRemoveBtnClick(file: File)
}