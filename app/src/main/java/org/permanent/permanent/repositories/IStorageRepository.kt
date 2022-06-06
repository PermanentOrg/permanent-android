package org.permanent.permanent.repositories

import org.permanent.permanent.network.IStringDataListener

interface IStorageRepository {

    fun getPaymentIntent(
        accountId: Int,
        accountEmail: String?,
        accountName: String?,
        isDonationAnonymous: Boolean?,
        donationAmount: Int,
        listener: IStringDataListener
    )
}