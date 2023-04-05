package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class ManageTagsViewModel(application: Application) : ObservableAndroidViewModel(application),
    Filterable {
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val unfilteredTags = MutableLiveData<List<Tag>>()
    private val tags = MutableLiveData<List<Tag>>()
    private val onAddButtonEvent = SingleLiveEvent<Void>()
    private val count = MutableLiveData<String>()
    private val archiveName = MutableLiveData((prefsHelper.getCurrentArchiveFullName() ?: "") + " Tags")

    val searchString = MutableLiveData<String>()

    init {
        reloadTags()

        searchString.observeForever {
            tags.value = unfilteredTags.value?.filter { tag ->
                tag.name.contains(it, true)
            }
        }
    }

    fun reloadTags() {
        val defaultArchiveId = prefsHelper.getDefaultArchiveId()

        requestTagsFor(defaultArchiveId)
    }

    fun getTagsCount(): LiveData<String> {
        return count
    }

    fun getArchiveName(): LiveData<String> {
        return archiveName
    }

    private fun requestTagsFor(archiveId: Int) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.getTagsByArchive(archiveId, object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                var newTags: MutableList<Tag> = mutableListOf()
                dataList?.let {
                    for (data in it) {
                        data.TagVO?.let { tagVO ->
                            newTags.add(Tag(tagVO))
                        }
                    }
                }
                unfilteredTags.value = newTags

                tags.value = unfilteredTags.value?.filter { tag: Tag ->
                    tag.name.contains(searchString.value ?: "", true)
                }
                count.value = "(" + tags.value?.size.toString() + ")"
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    // A function to call tagRepository.deleteTags() and handle the response.
    // This function is called from the fragment. And it's being passed a Tag object.
    fun deleteTag(tag: Tag) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.deleteTags(listOf(tag), object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                reloadTags()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun onAddButtonPressed() {
        onAddButtonEvent.call()
    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }

    fun getShowMessage(): LiveData<String> = showMessage
    fun getTags(): MutableLiveData<List<Tag>> = tags
    fun getOnAddButtonEvent(): LiveData<Void> = onAddButtonEvent
}