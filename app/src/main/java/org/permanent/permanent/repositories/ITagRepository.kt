package org.permanent.permanent.repositories

import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITagListener

interface ITagRepository {

    fun getTagsByArchive(archiveId: Int, listener: IDataListener)

    fun createOrLinkTags(tags: List<Tag>, recordId: Int, listener: ITagListener)

    fun unlinkTags(tags: List<Tag>, recordId: Int, listener: IResponseListener)

    fun deleteTags(tags: List<Tag>, listener: IResponseListener)

    fun updateTag(tag: Tag, archiveId: Int, listener: IResponseListener)
}