package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.EmailChip

class RedeemCodeViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var giftGB = MutableLiveData(0)
    private var note = ""
    private var showButtonEnabled = MutableLiveData(false)
    private var onGiftStorageSent = SingleLiveEvent<Void?>()

    private val emails = MutableLiveData<SnapshotStateList<EmailChip>>(mutableStateListOf())

    fun getIsBusy() = isBusy

    fun getGiftGB() = giftGB

    fun getNote() = note

    fun getEmails() = emails

    fun getShowButtonEnabled() = showButtonEnabled

    fun getOnGiftStorageSent() = onGiftStorageSent
}
