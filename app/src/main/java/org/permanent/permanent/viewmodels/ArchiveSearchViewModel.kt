package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository

class ArchiveSearchViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var searchJob: Job? = null
    private val isBusy = MutableLiveData(false)
    val currentSearchQuery = MutableLiveData<String>()
    private val existsArchives = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private val onArchivesRetrieved = SingleLiveEvent<List<Archive>>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString()
        if (currentSearchQuery.value.isNullOrEmpty()) {
            searchJob?.cancel()
            existsArchives.value = false
        }
        searchDebounced()
    }

    private fun searchDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            searchArchives()
        }
    }

    fun searchArchives() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val query = currentSearchQuery.value
        if (!query.isNullOrEmpty()) {
            isBusy.value = true
            archiveRepository.searchArchive(query, object : IDataListener {

                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    existsArchives.value = !dataList.isNullOrEmpty()
                    dataList?.let {
                        val archives: MutableList<Archive> = ArrayList()
                        for (data in it) {
                            val archive = Archive(data.ArchiveVO)
                            archive.isPopular = true
                            archives.add(archive)
                        }
                        onArchivesRetrieved.value = archives
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        } else {
            existsArchives.value = false
        }
    }

    fun sharePublicArchive(archive: Archive) {
        val sharableLink = BuildConfig.BASE_URL + "p/archive/" + archive.number +
                "/profile"
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_link_share_link_title), sharableLink
        )
        clipboard.setPrimaryClip(clip)
        showMessage.value = appContext.getString(R.string.share_link_link_copied)
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getExistsRecords(): MutableLiveData<Boolean> = existsArchives

    fun getOnShowMessage(): MutableLiveData<String> = showMessage
    fun getOnShowError(): MutableLiveData<String> = showError

    fun getOnArchivesRetrieved(): MutableLiveData<List<Archive>> = onArchivesRetrieved

    companion object {
        private const val SEARCH_DELAY_MILLIS = 300L
    }
}
