package org.permanent.permanent.network

import org.permanent.permanent.network.models.LegacyContact

interface ILegacyContactListener {

    fun onSuccess(contact: LegacyContact)

    fun onFailed(error: String?)
}