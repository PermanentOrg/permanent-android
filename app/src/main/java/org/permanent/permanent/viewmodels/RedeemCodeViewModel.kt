package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.network.IPromoListener
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IStorageRepository
import org.permanent.permanent.repositories.StorageRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class RedeemCodeViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    val showError = MutableLiveData<Boolean?>()
    val showSuccess = MutableLiveData<Boolean?>()
    private val isBusy = MutableLiveData(false)
    var code by mutableStateOf("")
        private set
    private var showButtonEnabled = MutableLiveData(false)
    private var onGiftStorageRedeemed = MutableLiveData<Int>()
    val storageRepository: IStorageRepository = StorageRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun updateEnteredCode(enteredCode: String) {
        code = enteredCode
        showButtonEnabled.value = code.isNotEmpty()
    }

    fun onRedeemButtonClick() {
        isBusy.value = true
        storageRepository.redeemGiftCode(code, object : IPromoListener {

            override fun onSuccess(promoSizeInMB: Int) {
                isBusy.value = false
                onGiftStorageRedeemed.value = promoSizeInMB
                showSuccess.value = true
                sendEvent(AccountEventAction.SUBMIT_PROMO)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = true }
                updateEnteredCode("")
            }
        })
    }

    fun sendEvent(action: AccountEventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun getIsBusy() = isBusy

    fun getShowButtonEnabled() = showButtonEnabled

    fun getOnGiftRedeemed() = onGiftStorageRedeemed
}
