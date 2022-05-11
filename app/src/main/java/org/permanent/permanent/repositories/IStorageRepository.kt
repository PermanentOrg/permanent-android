package org.permanent.permanent.repositories

import org.permanent.permanent.network.IStringDataListener

interface IStorageRepository {

    fun getPaymentIntent(
        accountId: Int,
        accountEmail: String?,
        donationAmount: Int,
        listener: IStringDataListener
    )
}