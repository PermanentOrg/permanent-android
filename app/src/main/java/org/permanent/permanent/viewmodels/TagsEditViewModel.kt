package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl

class TagsEditViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private var appContext: Context? = application.applicationContext
    private lateinit var fileData: FileData
    private val showMessage = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val onTagsRetrieved = MutableLiveData<List<Tag>>()
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
        requestTagsFor(fileData.archiveId)
    }

    private fun requestTagsFor(archiveId: Int) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.getTagsByArchive(archiveId, object : IDataListener {

            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                dataList?.let {
                    val tags: MutableList<Tag> = ArrayList()
                    for (data in it) {
                        data.TagVO?.let { tagVO -> tags.add(Tag(tagVO)) }
                    }
                    onTagsRetrieved.value = tags
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun getCurrentFileData(): FileData = fileData

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsBusy(): LiveData<Boolean> = isBusy

    fun getOnTagsRetrieved(): LiveData<List<Tag>> = onTagsRetrieved
}
