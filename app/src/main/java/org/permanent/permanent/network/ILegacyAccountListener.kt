package org.permanent.permanent.network

import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.models.LegacySteward

interface ILegacyAccountListener {
    fun onSuccess(legacyStewards: List<LegacySteward>)

    fun onFailed(error: String?)
}