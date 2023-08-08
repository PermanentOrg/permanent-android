package org.permanent.permanent.repositories

import org.permanent.permanent.network.ILegacyAccountListener
import org.permanent.permanent.network.ILegacyArchiveListener

interface ILegacyPlanningRepository {

    fun getLegacyContact(listener: ILegacyAccountListener)

    fun getArchiveSteward(archiveId: Int, listener: ILegacyArchiveListener)
}