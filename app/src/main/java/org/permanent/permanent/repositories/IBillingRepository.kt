package org.permanent.permanent.repositories

import org.permanent.permanent.network.IBillingListener
import org.permanent.permanent.network.models.StorageGift

interface IBillingRepository {

    fun send(gift: StorageGift, listener: IBillingListener)
}