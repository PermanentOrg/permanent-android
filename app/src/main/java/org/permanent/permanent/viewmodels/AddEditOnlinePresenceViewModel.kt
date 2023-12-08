package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.EventType
import org.permanent.permanent.EventsManager
import org.permanent.permanent.R
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AddEditOnlinePresenceViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()
    private val onBackRequest = SingleLiveEvent<Void?>()
    private val onlinePresence = MutableLiveData("")
    private var socialMediaProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var isEdit: Boolean? = false
    private var isAddEmail: Boolean? = false

    fun displayProfileItem(profileItem: ProfileItem?, isEdit: Boolean?, isAddEmail: Boolean?) {
        socialMediaProfileItem = profileItem
        profileItem?.string1?.let { onlinePresence.value = it }
        isEdit?.let { this.isEdit = isEdit }
        isAddEmail?.let { this.isAddEmail = isAddEmail }
    }

    fun onOnlinePresenceTextChanged(text: Editable) {
        onlinePresence.value = text.toString()
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val onlinePresenceValue = onlinePresence.value?.trim()
        socialMediaProfileItem?.let {
            if (!it.string1.contentEquals(onlinePresenceValue)) {
                it.string1 = onlinePresenceValue
                addUpdateProfileItem(it)
            }
        } ?: run {
            if (onlinePresenceValue?.isNotEmpty() == true) {
                val addItem = ProfileItem()
                addItem.archiveId = prefsHelper.getCurrentArchiveId()
                if (isAddEmail == true) addItem.fieldName = ProfileItemName.EMAIL
                else addItem.fieldName = ProfileItemName.SOCIAL_MEDIA
                addItem.string1 = onlinePresenceValue
                addUpdateProfileItem(addItem)
            }
        }
    }

    private fun addUpdateProfileItem(profileItemToUpdate: ProfileItem) {
        isBusy.value = true
        profileRepository.safeAddUpdateProfileItems(
            listOf(profileItemToUpdate), false,
            object : IProfileItemListener {
                override fun onSuccess(profileItem: ProfileItem) {
                    EventsManager(appContext).sendToMixpanel(EventType.EditArchiveProfile)
                    isBusy.value = false
                    profileItemToUpdate.id = profileItem.id
                    showMessage.value = appContext.getString(
                        R.string.add_edit_online_presence_success,
                        if (isEdit == true) appContext.getString(R.string.edited)
                        else appContext.getString(R.string.added)
                    )
                    onBackRequest.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showError.value = error
                }
            })
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage
    fun getShowError(): LiveData<String> = showError

    fun getOnBackRequest(): LiveData<Void?> = onBackRequest

    fun getIsOnAddEmail(): Boolean? = isAddEmail

    fun getOnlinePresence(): LiveData<String> = onlinePresence
}