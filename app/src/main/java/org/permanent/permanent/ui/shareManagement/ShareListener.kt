package org.permanent.permanent.ui.shareManagement

import org.permanent.permanent.models.Share

interface ShareListener {

    fun onEditClick(share: Share)

    fun onApproveClick(share: Share)

    fun onDenyClick(share: Share)
}