package org.permanent.permanent.repositories

import org.permanent.permanent.network.IPromoListener
import org.permanent.permanent.network.IStringDataListener

interface IStorageRepository {

    fun createStoragePurchase(amountInUSD: Int, listener: IStringDataListener)

    fun redeemGiftCode(code: String, listener: IPromoListener)
}
