package org.permanent.permanent.network

import org.permanent.permanent.network.models.ArchiveSteward

interface ILegacyArchiveListener {
    fun onSuccess(profileItem: List<ArchiveSteward>)

    fun onFailed(error: String?)
}