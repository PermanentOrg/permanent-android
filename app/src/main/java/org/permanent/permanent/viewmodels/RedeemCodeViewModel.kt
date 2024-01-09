package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.IStorageRepository
import org.permanent.permanent.repositories.StorageRepositoryImpl

class RedeemCodeViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    var code by mutableStateOf("")
        private set
    private var showButtonEnabled = MutableLiveData(false)
    private var onGiftStorageRedeemed = MutableLiveData<Int>()
    val storageRepository: IStorageRepository = StorageRepositoryImpl(application)

    fun updateEnteredCode(enteredCode: String) {
        code = enteredCode
        showButtonEnabled.value = code.isNotEmpty()
    }

    fun onRedeemButtonClick() {
        isBusy.value = true
        storageRepository.redeemGiftCode(code, object : IResponseListener {

            override fun onSuccess(message: String?) {
                isBusy.value = false
                onGiftStorageRedeemed.value = 4
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
                updateEnteredCode("")
            }
        })
    }

    fun getIsBusy() = isBusy

    fun getShowButtonEnabled() = showButtonEnabled

    fun getOnGiftRedeemed() = onGiftStorageRedeemed
}
