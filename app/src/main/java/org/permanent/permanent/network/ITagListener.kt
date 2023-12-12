package org.permanent.permanent.network

import org.permanent.permanent.models.Tag

interface ITagListener {

    fun onSuccess(createdTag: Tag?)

    fun onFailed(error: String?)
}