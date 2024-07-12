package org.permanent.permanent.repositories

import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IResponseListener

interface StelaAccountRepository {

    fun addRemoveTags(tags: Tags, listener: IResponseListener)
}