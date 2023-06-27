package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onArchiveSwitchedToCurrent = SingleLiveEvent<Void>()
    private val showError = MutableLiveData<String>()
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun switchArchiveToCurrent() {
        prefsHelper.getCurrentArchiveNr()?.let { currentArchiveNr ->
            archiveRepository.switchToArchive(currentArchiveNr, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    onArchiveSwitchedToCurrent.call()
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getOnArchiveSwitchedToCurrent(): MutableLiveData<Void> = onArchiveSwitchedToCurrent
    fun getShowError(): LiveData<String> = showError
}