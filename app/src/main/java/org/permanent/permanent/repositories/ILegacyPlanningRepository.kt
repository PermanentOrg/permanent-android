package org.permanent.permanent.repositories

import org.permanent.permanent.network.IArchiveStewardsListener
import org.permanent.permanent.network.ILegacyContactListener
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.IArchiveStewardListener
import org.permanent.permanent.network.models.LegacyContact

interface ILegacyPlanningRepository {

    fun addLegacyContact(legacyContact: LegacyContact, listener: ILegacyContactListener)

    fun editLegacyContact(legacyContactId: String, legacyContact: LegacyContact, listener: ILegacyContactListener)

    fun getLegacyContact(listener: ILegacyContactsListener)

    fun addArchiveSteward(archiveSteward: ArchiveSteward, listener: IArchiveStewardListener)

    fun editArchiveSteward(directiveId: String, archiveSteward: ArchiveSteward, listener: IArchiveStewardListener)

    fun getArchiveSteward(archiveId: Int, listener: IArchiveStewardsListener)
}