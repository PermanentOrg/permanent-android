package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.IStringDataListener
import org.permanent.permanent.repositories.IStorageRepository
import org.permanent.permanent.repositories.StorageRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper


class AddStorageViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    val amount = MutableLiveData(DONATION_AMOUNT_DEFAULT_VALUE)
    val gbEndowed =
        MutableLiveData(appContext.getString(R.string.storage_gb_endowed, GB_ENDOWED_DEFAULT_VALUE))
    private val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onPaymentIntentRetrieved = SingleLiveEvent<String>()
    val storageRepository: IStorageRepository = StorageRepositoryImpl(application)

    fun getPaymentIntent() {
        val amountString = amount.value
        val amountValue =
            if (amountString != null && amountString.isNotEmpty()) amountString.toInt() else 0

        if (amountValue != 0) {
            isBusy.value = true
            storageRepository.getPaymentIntent(prefsHelper.getAccountId(),
                prefsHelper.getAccountEmail(),
                prefsHelper.getAccountName(),
                false,
                amountValue * 100,
                object : IStringDataListener {

                    override fun onSuccess(data: String?) {
                        isBusy.value = false
                        onPaymentIntentRetrieved.value = data
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showError.value = it }
                    }
                })
        }
    }

    fun onAmountTextChanged(amount: Editable) {
        val amountString = amount.toString()
        this.amount.value = amountString
        val enteredAmount = if (amountString.isNotEmpty()) amountString.toInt() else 0
        this.gbEndowed.value = appContext.getString(
            R.string.storage_gb_endowed,
            if (enteredAmount >= 10) (enteredAmount / 10).toString() else "0"
        )
    }

    fun getOnPaymentIntentRetrieved(): LiveData<String> = onPaymentIntentRetrieved
    fun getOnError(): LiveData<String> = showError
    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    companion object {
        private const val DONATION_AMOUNT_DEFAULT_VALUE = "10"
        private const val GB_ENDOWED_DEFAULT_VALUE = "1"
        const val DONATION_AMOUNT_10_VALUE = "$10"
        const val DONATION_AMOUNT_20_VALUE = "$20"
        const val DONATION_AMOUNT_50_VALUE = "$50"
    }
}