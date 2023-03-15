package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
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
    private val tags = MutableLiveData<List<Tag>>()

    init {
        val defaultArchiveId = prefsHelper.getDefaultArchiveId()

        requestTagsFor(defaultArchiveId)
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
                tags.value = newTags
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }

    fun getShowMessage(): LiveData<String> = showMessage
    fun getTags(): MutableLiveData<List<Tag>> = tags
}