package org.permanent.permanent.repositories

import org.permanent.permanent.network.IDataListener

interface ITagRepository {

    fun getTagsByArchive(archiveId: Int, listener: IDataListener)
}