package org.permanent.permanent.network.models

data class StoragePurchaseResponse(val data: StoragePurchaseData?) {
    data class StoragePurchaseData(val clientSecret: String?)
}
