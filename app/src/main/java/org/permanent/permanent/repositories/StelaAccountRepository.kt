package org.permanent.permanent.repositories

import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener

interface StelaAccountRepository {

    fun addRemoveTags(tags: Tags, listener: IResponseListener)

    fun getTwoFAMethod(listener: ITwoFAListener)
}