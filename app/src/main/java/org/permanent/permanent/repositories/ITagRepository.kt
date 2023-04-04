package org.permanent.permanent.repositories

import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface ITagRepository {

    fun getTagsByArchive(archiveId: Int, listener: IDataListener)

    fun createOrLinkTags(tags: List<Tag>, recordId: Int, listener: IResponseListener)

    fun unlinkTags(tags: List<Tag>, recordId: Int, listener: IResponseListener)

    fun deleteTags(tags: List<Tag>, listener: IResponseListener)
}