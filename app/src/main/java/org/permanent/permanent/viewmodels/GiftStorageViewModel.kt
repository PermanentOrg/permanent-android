package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.EmailChip
import org.permanent.permanent.ui.gbToBytes

class GiftStorageViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var spaceTotalBytes = MutableLiveData(0L)
    private var spaceLeftBytes = MutableLiveData(0L)
    private var spaceUsedPercentage = MutableLiveData(0)
    private var giftGB = MutableLiveData(0)
    private var giftBytes = MutableLiveData(0L)
    private var note = ""
    private var showInsufficientStorageText = MutableLiveData(false)
    private var showButtonEnabled = MutableLiveData(false)

    private val emails = MutableLiveData<SnapshotStateList<EmailChip>>(mutableStateListOf())

    fun setSpaceTotal(it: Long) {
        spaceTotalBytes.value = it
    }

    fun setSpaceLeft(it: Long) {
        spaceLeftBytes.value = it
    }

    fun setSpaceUsedPercentage(it: Int) {
        spaceUsedPercentage.value = it
    }

    fun onMinusBtnClick() {
        val giftGBValue = giftGB.value
        if (giftGBValue != null && giftGBValue > 0) {
            giftGB.value = giftGBValue.minus(1)
            giftGB.value?.let { giftBytes.value = gbToBytes(it) }
            checkToShowInsufficientStorageText()
            checkToShowButtonEnabled()
        }
    }

    fun onPlusBtnClick() {
        giftGB.value = giftGB.value?.plus(1)
        giftGB.value?.let { giftBytes.value = gbToBytes(it) }
        checkToShowInsufficientStorageText()
        checkToShowButtonEnabled()
    }

    fun addEmailChip(emailChip: EmailChip) {
        emails.value?.add(emailChip)
        emails.postValue(emails.value)
        checkToShowInsufficientStorageText()
        checkToShowButtonEnabled()
    }

    fun removeEmailChip(emailChip: EmailChip) {
        emails.value?.remove(emailChip)
        emails.postValue(emails.value)
        checkToShowInsufficientStorageText()
        checkToShowButtonEnabled()
    }

    private fun checkToShowInsufficientStorageText() {
        val emailNrValue = emails.value?.count()
        val giftBytesValue = giftBytes.value
        val spaceLeftBytesValue = spaceLeftBytes.value
        if (emailNrValue != null && giftBytesValue != null && spaceLeftBytesValue != null) {
            showInsufficientStorageText.value =
                emailNrValue * giftBytesValue > spaceLeftBytesValue
        }
    }

    private fun checkToShowButtonEnabled() {
        val emailNrValue = emails.value?.count()
        val giftGBValue = giftGB.value
        val showInsufficientStorageTextValue = showInsufficientStorageText.value

        showButtonEnabled.value =
            emailNrValue != null && emailNrValue > 0 && giftGBValue != null && giftGBValue > 0 && showInsufficientStorageTextValue == false
    }

    fun onSendGiftStorageClick(note: String) {

    }

    fun getSpaceTotal() = spaceTotalBytes

    fun getSpaceLeft() = spaceLeftBytes

    fun getSpaceUsedPercentage() = spaceUsedPercentage

    fun getGiftGB() = giftGB

    fun getGiftBytes() = giftBytes
    fun getNote() = note

    fun getEmails() = emails

    fun getShowInsufficientStorageText() = showInsufficientStorageText

    fun getShowButtonEnabled() = showButtonEnabled
}
