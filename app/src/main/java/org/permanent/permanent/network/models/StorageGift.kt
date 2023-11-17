package org.permanent.permanent.network.models

class StorageGift() {
    var recipientEmails: List<String>? = null
    var storageAmount: Int? = null
    var note: String? = null

    constructor(recipientEmails: List<String>, storageAmount: Int, note: String?) : this() {
        this.recipientEmails = recipientEmails
        this.storageAmount = storageAmount
        this.note = note
    }
}