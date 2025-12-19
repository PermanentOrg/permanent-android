package org.permanent.permanent.network

import org.permanent.permanent.network.models.ShareLinkVO

interface ILinkListener {
    fun onSuccess(shareLink: ShareLinkVO?)

    fun onFailed(error: String?)
}