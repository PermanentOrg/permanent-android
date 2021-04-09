package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class TagsEditViewModel(application: Application) : ObservableAndroidViewModel(application),
    Filterable {

    private val appContext = application.applicationContext
    private lateinit var fileData: FileData
    private val newTagName = MutableLiveData<String>()
    private val showMessage = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val onTagsFiltered = MutableLiveData<List<Tag>>()
    private val onTagsUpdated = MutableLiveData<FileData>()
    private val allTags = ArrayList<Tag>()
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
                    for (data in it) {
                        data.TagVO?.let { tagVO ->
                            val archiveTag = Tag(tagVO)
                            archiveTag.isCheckedOnServer = fileData.getTagIds().contains(archiveTag.tagId)
                            archiveTag.isCheckedOnLocal = archiveTag.isCheckedOnServer
                            allTags.add(archiveTag)
                        }
                    }
                    onTagsFiltered.value = allTags
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun updateTagsOnServer() {
        val newCheckedTags = ArrayList<Tag>()
        val newUncheckedTags = ArrayList<Tag>()
        for (tag in allTags) {
            val checkedOnLocal = tag.isCheckedOnLocal
            if (checkedOnLocal != tag.isCheckedOnServer) {
                if (checkedOnLocal) newCheckedTags.add(tag) else newUncheckedTags.add(tag)
            }
        }
        when {
            newCheckedTags.isNotEmpty() ->
                saveNewCheckedTags(newCheckedTags, newUncheckedTags, fileData.recordId)
            newUncheckedTags.isNotEmpty() ->
                saveNewUncheckedTags(newUncheckedTags, fileData.recordId)
            else -> onTagsUpdated.value = fileData
        }
    }

    private fun saveNewCheckedTags(
        newCheckedTags: ArrayList<Tag>,
        newUncheckedTags: ArrayList<Tag>,
        recordId: Int
    ) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.createOrLinkTags(newCheckedTags, recordId, object : IResponseListener {

            override fun onSuccess(message: String?) {
                isBusy.value = false
                if (newUncheckedTags.isNotEmpty())
                    saveNewUncheckedTags(newUncheckedTags, fileData.recordId)
                else requestUpdatedFileData()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                if (newUncheckedTags.isNotEmpty())
                    saveNewUncheckedTags(newUncheckedTags, fileData.recordId)
                showMessage.value = error
            }
        })
    }

    private fun saveNewUncheckedTags(uncheckedTags: ArrayList<Tag>, recordId: Int) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.unlinkTags(uncheckedTags, recordId, object : IResponseListener {

            override fun onSuccess(message: String?) {
                isBusy.value = false
                requestUpdatedFileData()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    private fun requestUpdatedFileData() {
        val folderLinkId = fileData.folderLinkId
        val archiveNr = fileData.archiveNr
        val archiveId = fileData.archiveId
        val recordId = fileData.recordId

        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveNr?.let {
            fileRepository.getRecord(folderLinkId, it, archiveId, recordId
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    response.body()?.getFileData()?.let { newFileData -> fileData = newFileData }
                    showMessage.value = appContext.getString(R.string.file_tags_update_success)
                    onTagsUpdated.value = fileData
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    showMessage.value = t.message
                }
            })
        }
    }

    fun onNewTagNameTextChanged(textEditable: Editable) {
        val text = textEditable.toString()
        newTagName.value = text
        filter.filter(text)
    }

    fun onAddClick() {
        val newTagNameValue = newTagName.value
        if (!newTagNameValue.isNullOrEmpty()) {
            allTags.add(0, Tag(null, newTagNameValue))
            onTagsFiltered.value = allTags
            newTagName.value = ""
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charSearch = charSequence.toString()
                val filterResults = FilterResults()

                filterResults.values = if (charSearch.isEmpty()) allTags else {
                    val resultList = ArrayList<Tag>()
                    for (tag in allTags) {
                        if (tag.name.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(tag)
                        }
                    }
                    resultList
                }
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                onTagsFiltered.value = results?.values as ArrayList<Tag>
            }
        }
    }

    fun getNewTagName(): LiveData<String> = newTagName

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsBusy(): LiveData<Boolean> = isBusy

    fun getOnTagsFiltered(): LiveData<List<Tag>> = onTagsFiltered

    fun getOnTagsUpdated(): LiveData<FileData> = onTagsUpdated
}
