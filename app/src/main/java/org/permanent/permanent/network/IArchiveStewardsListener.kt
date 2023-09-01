package org.permanent.permanent.network

import org.permanent.permanent.network.models.ArchiveSteward

interface IArchiveStewardsListener {
    fun onSuccess(archiveStewards: List<ArchiveSteward>)

    fun onFailed(error: String?)
}