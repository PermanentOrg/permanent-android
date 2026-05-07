package org.permanent.permanent.ui.shares

import org.permanent.permanent.Constants

sealed class SharePreviewNavEvent(val tabPosition: Int, val itemId: Int?) {
    class OpenSharedByMe(folderId: Int?) :
        SharePreviewNavEvent(Constants.POSITION_SHARED_BY_ME_FRAGMENT, folderId)

    class OpenSharedWithMe(itemId: Int?) :
        SharePreviewNavEvent(Constants.POSITION_SHARED_WITH_ME_FRAGMENT, itemId)
}
