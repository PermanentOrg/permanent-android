package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITagListener
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AddEditTagViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private val onDismissEvent = SingleLiveEvent<Void?>()
    private val onUpdateSuccessEvent = SingleLiveEvent<Void?>()
    private val onUpdateFailedEvent = MutableLiveData<String>()
    private var tag: Tag? = null

    val tagName = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    val prefsHelper = PreferencesHelper(
        PermanentApplication.instance.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    )

    fun setTag(tag: Tag?) {
        this.tag = tag
        if (tag != null) {
            tagName.value = tag.name
        }
    }

    fun getTag(): Tag? {
        return tag
    }

    fun onCancelBtnClick() {
        onDismissEvent.call()
    }

    fun onUpdateBtnClick() {
        if ((isBusy.value != null && isBusy.value!!) || tagName.value.isNullOrEmpty()) {
            return
        }

        if (tag != null) {
            tag!!.name = tagName.value!!
            updateTag(tag!!)
        } else {
            val newTag = Tag(null, tagName.value!!)
            createTag(newTag)
        }
    }

    fun createTag(newTag: Tag) {
        val tags: List<Tag> = listOf(newTag)

        isBusy.value = true
        tagRepository.createOrLinkTags(tags, 0, object : ITagListener {

            override fun onSuccess(createdTag: Tag?) {
                isBusy.value = false
                onUpdateSuccessEvent.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onUpdateFailedEvent.value = error
            }
        })
    }

    fun updateTag(newTag: Tag) {
        val defaultArchiveId = prefsHelper.getDefaultArchiveId()

        isBusy.value = true
        tagRepository.updateTag(newTag, defaultArchiveId, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                onUpdateSuccessEvent.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onUpdateFailedEvent.value = error
            }
        })
    }

    fun getOnDismissEvent(): SingleLiveEvent<Void?> {
        return onDismissEvent
    }

    fun getOnUpdateSuccessEvent(): SingleLiveEvent<Void?> {
        return onUpdateSuccessEvent
    }

    fun getOnUpdateFailedEvent(): MutableLiveData<String> {
        return onUpdateFailedEvent
    }
}