package org.permanent.permanent.repositories

import org.permanent.permanent.network.ILegacyArchiveListener
import org.permanent.permanent.network.ILegacyContactListener
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.LegacyContact

interface ILegacyPlanningRepository {

    fun addLegacyContact(legacyContact: LegacyContact, listener: ILegacyContactListener)

    fun editLegacyContact(legacyContactId: String, legacyContact: LegacyContact, listener: ILegacyContactListener)

    fun getLegacyContact(listener: ILegacyContactsListener)

    fun getArchiveSteward(archiveId: Int, listener: ILegacyArchiveListener)
}