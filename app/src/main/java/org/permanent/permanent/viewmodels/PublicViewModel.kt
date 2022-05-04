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
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.util.ArrayList

class PublicViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var currentArchive: Archive? = null
    private var currentRecord: Record? = null
    private val currentBannerThumb = MutableLiveData<String>()
    private val currentArchiveThumb = MutableLiveData<String>()
    private val currentArchiveName = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var currentArchiveNr : String? = null

    fun setHeaderData(archive: Archive) {
        currentArchiveName.value = archive.fullName
        currentArchiveThumb.value = archive.thumbURL200
        currentBannerThumb.value = currentRecord?.thumbURL2000
    }

    fun getArchive(archiveNr: String?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true

        currentArchiveNr = archiveNr ?: prefsHelper.getCurrentArchiveNr()

        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {

                    for (datum in dataList) {
                        val archive = Archive(datum.ArchiveVO)
                        if (currentArchiveNr == archive.number) {
                            currentArchive = archive
                        }
                    }
                }
                getPublicRecords(currentArchiveNr)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })

    }

    fun getProfileBannerThumb(): MutableLiveData<String> = currentBannerThumb

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): MutableLiveData<String> = showMessage

    fun getPublicRecords(archiveNr: String?) {
        isBusy.value = true
        currentArchiveNr = archiveNr ?: prefsHelper.getCurrentArchiveNr()
        fileRepository.getPublicRoot(currentArchiveNr, object : IRecordListener {
            override fun onSuccess(record: Record) {
                isBusy.value = false
                currentRecord = record
                currentArchive?.let { setHeaderData(it) }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun updateBannerOrProfilePhoto(isFileForProfileBanner: Boolean, record: Record) {
        if (isFileForProfileBanner) {
            record.archiveNr?.let {
                isBusy.value = true
                fileRepository.updateProfileBanner(record, object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        currentBannerThumb.value = prefsHelper.getPublicRecordThumbURL2000()
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