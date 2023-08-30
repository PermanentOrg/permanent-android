package org.permanent.permanent.network.models

interface IArchiveStewardListener {

    fun onSuccess(archiveSteward: ArchiveSteward)

    fun onFailed(error: String?)
}