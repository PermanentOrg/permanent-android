package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.ITagListener
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl

class NewTagViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    val showError = MutableLiveData<String>()
    var newTagName by mutableStateOf("")
        private set
    var recentTagsTitle by mutableStateOf(appContext.getString(R.string.recent_tags, 0))
        private set
    var addButtonTitle by mutableStateOf(appContext.getString(R.string.add_x_tags, 0))
        private set
    private val records = mutableListOf<Record>()
    private val recentTags = MutableLiveData<SnapshotStateList<Tag>>(mutableStateListOf())
    private var selectedRecentTagsSize = MutableLiveData(0)
    private val onTagsAddedToSelection = MutableLiveData<List<Tag>>()
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)

    fun setRecords(records: ArrayList<Record>?) {
        records?.toMutableList()?.let { recordList ->
            this.records.addAll(recordList)
        }
    }

    fun setRecentTags(tags: ArrayList<Tag>?) {
        tags?.toMutableList()?.let { tagList ->
            this.recentTags.value?.addAll(tagList)
            selectedRecentTagsSize.value = tagList.filter { it.isSelected.value == true }.size
        }
    }

    fun updateNewTag(it: String) {
        newTagName = it
    }

    fun onPlusButtonClick() {
        if (isBusy.value == true) return

        val tags: List<Tag> = listOf(Tag(null, newTagName))

        isBusy.value = true
        tagRepository.createOrLinkTags(tags, 0, object : ITagListener {

            override fun onSuccess(createdTag: Tag?) {
                isBusy.value = false
                createdTag?.let {
                    it.isSelected.value = true
                    recentTags.value?.add(createdTag)
                    recentTags.postValue(recentTags.value)
                    newTagName = ""
                    selectedRecentTagsSize.value = selectedRecentTagsSize.value?.inc()
                    updateTagsTexts()
                } ?: { showError.value = appContext.getString(R.string.generic_error) }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
            }
        })
    }

    fun onTagClick(tag: Tag) {
        tag.isSelected.value?.let {
            selectedRecentTagsSize.value =
                if (tag.isSelected.value == true) selectedRecentTagsSize.value?.dec() else selectedRecentTagsSize.value?.inc()
            tag.isSelected.value = !it
            updateTagsTexts()
        }
    }

    private fun updateTagsTexts() {
        recentTagsTitle =
            appContext.getString(R.string.recent_tags, selectedRecentTagsSize.value)
        addButtonTitle = appContext.getString(R.string.add_x_tags, selectedRecentTagsSize.value)
    }

    fun onAddTagsButtonClick() {
        if (isBusy.value == true) return

        var appliedTagToRecordsNr = 0
        recentTags.value?.filter { it.isSelected.value == true }?.let { selectedRecentTags ->

            isBusy.value = true
            for (record in records) {
                record.recordId?.let { recordId ->
                    tagRepository.createOrLinkTags(
                        selectedRecentTags,
                        recordId,
                        object : ITagListener {

                            override fun onSuccess(createdTag: Tag?) {
                                appliedTagToRecordsNr++
                                if (appliedTagToRecordsNr == records.size) {
                                    isBusy.value = false
                                    onTagsAddedToSelection.value = selectedRecentTags
                                }
                            }

                            override fun onFailed(error: String?) {
                                isBusy.value = false
                                error?.let { showError.value = it }
                            }
                        })
                }
            }
        }
    }

    fun getRecentTags() = recentTags

    fun getOnTagsAddedToSelection() = onTagsAddedToSelection

    fun getIsBusy() = isBusy
}