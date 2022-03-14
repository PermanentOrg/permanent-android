package org.permanent.permanent.repositories

import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IProfileItemListener

interface IProfileRepository {

    fun getProfileItemsByArchive(archiveNr: String?, listener: IDataListener)

    fun safeAddUpdateProfileItems(profileItems: List<ProfileItem>, serializeNulls: Boolean, listener: IProfileItemListener)

    fun deleteProfileItem(profileItem: ProfileItem, listener: IProfileItemListener)
}