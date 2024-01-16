package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.bytesToCustomHumanReadableString
import org.permanent.permanent.ui.mbToBytes

class StorageMenuViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val showError = MutableLiveData<String>()
    val showSuccess = MutableLiveData<String?>(null)
    private val isBusy = MutableLiveData(false)
    private var spaceTotalBytes = MutableLiveData(0L)
    private var spaceLeftBytes = MutableLiveData(0L)
    private var spaceUsedBytes = MutableLiveData(0L)
    private var spaceUsedPercentage = MutableLiveData(0)

    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun setPromoSizeInMB(size: Int) {
        if (size != 0)
            showSuccess.value =
                bytesToCustomHumanReadableString(mbToBytes(size), true) + " of storage"
    }

    fun updateUsedStorage() {
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                val spaceTotal = account.spaceTotal
                val spaceLeft = account.spaceLeft
                if (spaceTotal != null && spaceLeft != null) {
                    spaceTotalBytes.value = spaceTotal
                    spaceLeftBytes.value = spaceLeft
                    val spaceUsed = spaceTotal - spaceLeft
                    spaceUsedBytes.value = spaceUsed
                    val spaceUsedPercentageFloat = spaceUsed.toFloat() / spaceTotal.toFloat() * 100
                    spaceUsedPercentage.value = spaceUsedPercentageFloat.toInt()
                }
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    fun getIsBusy() = isBusy

    fun getSpaceTotal() = spaceTotalBytes

    fun getSpaceLeft() = spaceLeftBytes

    fun getSpaceUsed() = spaceUsedBytes

    fun getSpaceUsedPercentage() = spaceUsedPercentage
}
