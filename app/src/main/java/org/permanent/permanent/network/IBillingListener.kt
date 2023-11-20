package org.permanent.permanent.network

import org.permanent.permanent.network.models.StorageGift

interface IBillingListener {

    fun onSuccess(gift: StorageGift)

    fun onFailed(error: String?)
}