package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl

class LinkSettingsViewModel(application: Application) : ObservableAndroidViewModel(application),
    DatePickerDialog.OnDateSetListener {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private lateinit var shareByUrl: ShareByUrl
    private val sharePreview = MutableLiveData(false)
    private val autoApprove = MutableLiveData(false)
    private val maxUses = MutableLiveData("0")
    private val expirationDate = MutableLiveData<String>()
    private val showDatePicker = SingleLiveEvent<Void>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setRecord(record: Record) {
        this.record = record
    }

    fun setShareByUrl(shareByUrl: ShareByUrl) {
        this.shareByUrl = shareByUrl
        sharePreview.value = shareByUrl.previewToggle == 1
        autoApprove.value = shareByUrl.autoApproveToggle == 1
        maxUses.value = shareByUrl.maxUses.toString()
        shareByUrl.expiresDT.let { expirationDate.value = it }
    }

    fun getSharePreview(): MutableLiveData<Boolean> {
        return sharePreview
    }

    fun onSharePreviewChanged(checked: Boolean) {
        this.sharePreview.value = checked
    }

    fun getAutoApprove(): MutableLiveData<Boolean> {
        return autoApprove
    }

    fun onAutoApproveChanged(checked: Boolean) {
        this.autoApprove.value = checked
    }

    fun getMaxUses(): MutableLiveData<String> {
        return maxUses
    }

    fun onMaxUsesChanged(maxUses: Editable) {
        this.maxUses.value = maxUses.toString().trim { it <= ' ' }
    }

    fun getExpirationDate(): MutableLiveData<String> {
        return expirationDate
    }

    fun getShowDatePicker(): LiveData<Void> {
        return showDatePicker
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun onExpirationDateClick() {
        showDatePicker.call()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        expirationDate.value = "$year-${month + 1}-$day"
    }

    fun onSaveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        shareByUrl.previewToggle = if (sharePreview.value == false) 0 else 1
        shareByUrl.autoApproveToggle = if (autoApprove.value == false) 0 else 1
        shareByUrl.maxUses = if (maxUses.value.isNullOrBlank()) 0 else maxUses.value!!.toInt()
        shareByUrl.expiresDT = expirationDate.value

        isBusy.value = true
        shareRepository.modifyShareLink(shareByUrl.getShareByUrlVO(), ShareRequestType.UPDATE,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
    }
}