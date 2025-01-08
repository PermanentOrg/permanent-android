package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onArchiveSwitchedToCurrent = SingleLiveEvent<Void?>()
    private val showError = MutableLiveData<String>()
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun switchArchiveToCurrent() {
        prefsHelper.getCurrentArchiveNr()?.let { currentArchiveNr ->
            archiveRepository.switchToArchive(currentArchiveNr, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    getTwoFAMethod()
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getTwoFAMethod() {
        stelaAccountRepository.getTwoFAMethod(object : ITwoFAListener {

            override fun onSuccess(twoFAVOList: List<TwoFAVO>?) {
                prefsHelper.setIsTwoFAEnabled(twoFAVOList != null)
                if (twoFAVOList != null) prefsHelper.setTwoFAList(twoFAVOList)

                onArchiveSwitchedToCurrent.call()
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    fun getOnArchiveSwitchedToCurrent(): MutableLiveData<Void?> = onArchiveSwitchedToCurrent
    fun getShowError(): LiveData<String> = showError
}