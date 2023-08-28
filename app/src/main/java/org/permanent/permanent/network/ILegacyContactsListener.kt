package org.permanent.permanent.network

import org.permanent.permanent.network.models.LegacyContact

interface ILegacyContactsListener {
    fun onSuccess(legacyContacts: List<LegacyContact>)

    fun onFailed(error: String?)
}