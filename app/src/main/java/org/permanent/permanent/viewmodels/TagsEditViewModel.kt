package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import android.widget.Filter
import android.widget.Filterable
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
import java.util.*
import kotlin.collections.ArrayList

class TagsEditViewModel(application: Application) : ObservableAndroidViewModel(application),
    Filterable {

    private var appContext: Context? = application.applicationContext
    private lateinit var fileData: FileData
    private val newTagName = MutableLiveData<String>()
    private val showMessage = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val onTagsUpdate = MutableLiveData<List<Tag>>()
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
                            archiveTag.isChecked = fileData.tags?.contains(archiveTag) == true
                            allTags.add(archiveTag)
                        }
                    }
                    onTagsUpdate.value = allTags
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun onNewTagNameTextChanged(textEditable: Editable) {
        val text = textEditable.toString()
        newTagName.value = text
        filter.filter(text)
    }

    fun onAddClick() {
        val newTagNameValue = newTagName.value

        newTagNameValue?.let { allTags.add(Tag(null, it)) }
        onTagsUpdate.value = allTags
        newTagName.value = ""
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
                onTagsUpdate.value = results?.values as ArrayList<Tag>
            }
        }
    }

    fun getCurrentFileData(): FileData = fileData

    fun getNewTagName(): LiveData<String> = newTagName

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsBusy(): LiveData<Boolean> = isBusy

    fun getOnTagsUpdate(): LiveData<List<Tag>> = onTagsUpdate
}
