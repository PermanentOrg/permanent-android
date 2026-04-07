package org.permanent.permanent.repositories

import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.ILinkListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.TwoFAVO

interface StelaAccountRepository {

    fun addRemoveTags(tags: Tags, listener: IResponseListener)

    fun getTwoFAMethod(listener: ITwoFAListener)

    fun sendEnableCode(twoFAVO: TwoFAVO, listener: IResponseListener)

    fun enableTwoFactor(twoFAVO: TwoFAVO, listener: IResponseListener)

    fun sendDisableCode(twoFAVO: TwoFAVO, listener: IResponseListener)

    fun disableTwoFactor(twoFAVO: TwoFAVO, listener: IResponseListener)

    fun getShareLink(
        shareLinkIds: List<Int>? = null,
        shareTokens: List<String>? = null,
        listener: ILinkListener
    )

    fun getFolderChildren(
        shareToken: String?,
        folderId: Int,
        pageSize: Int = 99999999,
        listener: IFolderChildrenListener
    )

    fun generateShareLink(shareLinkVO: ShareLinkVO, listener: ILinkListener)

    fun updateShareLink(shareLinkVO: ShareLinkVO, listener: IResponseListener)

    fun deleteShareLink(shareLinkId: String, listener: IResponseListener)
}