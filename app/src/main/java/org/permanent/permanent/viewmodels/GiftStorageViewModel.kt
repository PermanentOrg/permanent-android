package org.permanent.permanent.viewmodels

import android.app.Application
import android.view.animation.Transformation
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.EmailChip
import org.permanent.permanent.network.models.ArchiveSteward
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
    private var showInsufficientStorageText = MediatorLiveData(false)

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
        }
    }

    fun onPlusBtnClick() {
        giftGB.value = giftGB.value?.plus(1)
        giftGB.value?.let { giftBytes.value = gbToBytes(it) }
        checkToShowInsufficientStorageText()
    }

    fun addEmailChip(emailChip: EmailChip) {
        emails.value?.add(emailChip)
        emails.postValue(emails.value)
    }

    fun removeEmailChip(emailChip: EmailChip) {
        emails.value?.remove(emailChip)
        emails.postValue(emails.value)
    }

    private fun checkToShowInsufficientStorageText() : Boolean {
        val emailNrValue = emails.value?.size
        val giftBytesValue = giftBytes.value
        val spaceLeftBytesValue = spaceLeftBytes.value
        if (emailNrValue != null && giftBytesValue != null && spaceLeftBytesValue != null) {
            return emailNrValue * giftBytesValue > spaceLeftBytesValue
        }
        return false
    }

    fun getSpaceTotal() = spaceTotalBytes

    fun getSpaceLeft() = spaceLeftBytes

    fun getSpaceUsedPercentage() = spaceUsedPercentage

    fun getGiftGB() = giftGB

    fun getGiftBytes() = giftBytes

    fun getNote() = note

    fun getEmails() = emails

    fun getShowInsufficientStorageText() = showInsufficientStorageText
}
