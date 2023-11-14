package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
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
        }
    }

    fun onPlusBtnClick() {
        giftGB.value = giftGB.value?.plus(1)
        giftGB.value?.let { giftBytes.value = gbToBytes(it) }
    }

    fun onSendGiftStorageClick() {

    }

    fun getSpaceTotal() = spaceTotalBytes

    fun getSpaceLeft() = spaceLeftBytes

    fun getSpaceUsedPercentage() = spaceUsedPercentage

    fun getGiftGB() = giftGB

    fun getGiftBytes() = giftBytes
    fun getNote() = note
}
