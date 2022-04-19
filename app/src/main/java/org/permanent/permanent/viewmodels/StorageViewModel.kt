package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.IStorageRepository
import org.permanent.permanent.repositories.StorageRepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StorageViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val amount = MutableLiveData(DONATION_AMOUNT_DEFAULT_VALUE)
    val gbEndowed =
        MutableLiveData(appContext.getString(R.string.storage_gb_endowed, GB_ENDOWED_DEFAULT_VALUE))
    private val showMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onClientSecretRetrieved = SingleLiveEvent<String>()
    val storageRepository: IStorageRepository = StorageRepositoryImpl(application)

    fun getClientSecret() {
        val amountSplits = amount.value?.split("$")
        val amountValue =
            if (amountSplits != null && amountSplits.size > 1 && amountSplits[1].isNotEmpty()) amountSplits[1].toInt() else 0

        if (amountValue != 0) {
            isBusy.value = true
            NetworkClient.instance().getClientSecret(amountValue * 100)
                .enqueue(object : Callback<ResponseVO> {

                    override fun onResponse(
                        call: Call<ResponseVO>,
                        responseVO: Response<ResponseVO>
                    ) {
                        isBusy.value = false
                        if (responseVO.isSuccessful) {
                            onClientSecretRetrieved.value = responseVO.body()?.client_secret
                        }
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        isBusy.value = false
                        showMessage.value = t.message
                    }
                })
        }
    }

    fun onAmountTextChanged(amount: Editable) {
        val amountSplits = amount.toString().split("$")
        val amountString = if (amountSplits.size > 1) amountSplits[1] else ""
        this.amount.value = "$$amountString"
        val enteredAmount = if (amountString.isNotEmpty()) amountString.toInt() else 0
        this.gbEndowed.value = appContext.getString(
            R.string.storage_gb_endowed,
            if (enteredAmount >= 10) (enteredAmount / 10).toString() else "0"
        )
    }

    fun getOnClientSecretRetrieved(): LiveData<String> = onClientSecretRetrieved
    fun getOnMessage(): LiveData<String> = showMessage
    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    companion object {
        private const val DONATION_AMOUNT_DEFAULT_VALUE = "$10"
        private const val GB_ENDOWED_DEFAULT_VALUE = "1"
        const val DONATION_AMOUNT_10_VALUE = "$10"
        const val DONATION_AMOUNT_20_VALUE = "$20"
        const val DONATION_AMOUNT_50_VALUE = "$50"
    }
}