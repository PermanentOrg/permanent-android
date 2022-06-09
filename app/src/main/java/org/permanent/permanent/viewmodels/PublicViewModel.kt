package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val profileBannerThumb = MutableLiveData<String>()
    private val currentArchiveThumb = MutableLiveData<String>()
    private val currentArchiveName = MutableLiveData<String>()
    private val onArchiveRetrieved = MutableLiveData<Archive>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun getArchive(archiveNr: String) {
        val archiveNrList = listOf(archiveNr)
        isBusy.value = true
        with(archiveRepository) {
            getArchivesByNr(archiveNrList, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        onArchiveRetrieved.value = Archive(dataList[0].ArchiveVO)
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun setArchive(currentArchive: Archive) {
        getPublicRoot(currentArchive)
    }

    fun getProfileBannerThumb(): MutableLiveData<String> = profileBannerThumb

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getOnArchiveRetrieved(): MutableLiveData<Archive> = onArchiveRetrieved

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): MutableLiveData<String> = showMessage
    fun getShowError(): MutableLiveData<String> = showError

    private fun getPublicRoot(archive: Archive) {
        isBusy.value = true
        fileRepository.getPublicRoot(archive.number, object : IRecordListener {
            override fun onSuccess(record: Record) {
                isBusy.value = false
                setHeaderData(archive, record.thumbURL2000)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
            }
        })
    }

    fun setHeaderData(archive: Archive, bannerThumbURL: String?) {
        currentArchiveName.value = archive.fullName
        currentArchiveThumb.value = archive.thumbURL200
        bannerThumbURL?.let { profileBannerThumb.value = it }
    }

    fun updateBannerOrProfilePhoto(isFileForProfileBanner: Boolean, record: Record) {
        if (isFileForProfileBanner) {
            record.archiveNr?.let {
                isBusy.value = true
                fileRepository.updateProfileBanner(record, object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        profileBannerThumb.value = prefsHelper.getPublicRecordThumbURL2000()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showMessage.value = it }
                    }
                })
            }
        } else {
            record.archiveNr?.let {
                isBusy.value = true
                archiveRepository.updateProfilePhoto(record, object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        currentArchiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showMessage.value = it }
                    }
                })
            }
        }
    }
}