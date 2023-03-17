package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl

class AddEditTagViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private val onDismissEvent = SingleLiveEvent<Void>()
    private val onUpdateSuccessEvent = SingleLiveEvent<Void>()
    private val onUpdateFailedEvent = MutableLiveData<String>()

    val tagName = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun onCancelBtnClick() {
        onDismissEvent.call()
    }

    fun onUpdateBtnClick() {
        if ((isBusy.value != null && isBusy.value!!) || tagName.value.isNullOrEmpty()) {
            return
        }

        val newTag = Tag(null, tagName.value!!)
        val tags: List<Tag> = listOf(newTag)

        isBusy.value = true
        tagRepository.createOrLinkTags(tags, 0, object : IResponseListener {
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

    fun getOnDismissEvent(): SingleLiveEvent<Void> {
        return onDismissEvent
    }

    fun getOnUpdateSuccessEvent(): SingleLiveEvent<Void> {
        return onUpdateSuccessEvent
    }

    fun getOnUpdateFailedEvent(): MutableLiveData<String> {
        return onUpdateFailedEvent
    }
}