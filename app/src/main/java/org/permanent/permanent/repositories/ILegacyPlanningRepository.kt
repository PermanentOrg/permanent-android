package org.permanent.permanent.repositories

import org.permanent.permanent.network.IResponseListener

interface ILegacyPlanningRepository {

    fun getLegacyContact(listener: IResponseListener)
}