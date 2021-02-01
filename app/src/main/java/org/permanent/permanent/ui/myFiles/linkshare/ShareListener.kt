package org.permanent.permanent.ui.myFiles.linkshare

import org.permanent.permanent.models.Share

interface ShareListener {

    fun onApproveClick(share: Share)

    fun onDenyClick(share: Share)
}