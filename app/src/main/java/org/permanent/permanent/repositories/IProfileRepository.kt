package org.permanent.permanent.repositories

import org.permanent.permanent.network.IDataListener

interface IProfileRepository {

    fun getProfileItemsByArchive(listener: IDataListener)
}