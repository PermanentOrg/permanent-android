package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AddEditOnlinePresenceViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String?>()
    private val showError = MutableLiveData<String?>()
    private val onBackRequest = SingleLiveEvent<Void>()
    private val socialMedia = MutableLiveData("")
    private var socialMediaProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl(application)
    private var isEdit: Boolean? = false
    private var isAddEmail: Boolean? = false

    fun displayProfileItem(profileItem: ProfileItem?, isEdit: Boolean?, isAddEmail: Boolean?) {
        socialMediaProfileItem = profileItem
        profileItem?.string1?.let { socialMedia.value = it }
        isEdit?.let { this.isEdit = isEdit}
        isAddEmail?.let { this.isAddEmail = isAddEmail}
    }

    fun onSocialMediaTextChanged(text: Editable) {
        socialMedia.value = text.toString()
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        socialMediaProfileItem?.let {
            if (!it.string1.contentEquals(socialMedia.value?.trim())) {
                it.string1 = socialMedia.value?.trim()
                addUpdateProfileItem(socialMediaProfileItem!!)
            }
        } ?: run {
            if (socialMedia.value?.trim()?.isNotEmpty() == true) {
                val addItem = ProfileItem ()
                addItem.archiveId = prefsHelper.getCurrentArchiveId()
                if(isAddEmail ==  true){
                    addItem.fieldName = ProfileItemName.EMAIL
                } else {
                    addItem.fieldName = ProfileItemName.SOCIAL_MEDIA
                }
                addItem.string1 = socialMedia.value?.trim()
                addUpdateProfileItem(addItem)
            }
        }
        onBackRequest.call()
    }

    private fun addUpdateProfileItem(profileItemToUpdate: ProfileItem) {
        isBusy.value = true
        profileRepository.safeAddUpdateProfileItems(
            profileItemToUpdate,
            object : IProfileItemListener {
                override fun onSuccess(profileItem: ProfileItem) {
                    isBusy.value = false
                    profileItemToUpdate.id = profileItem.id
                    if(isEdit == true) {
                        showMessage.value = appContext.getString(R.string.edit_social_media_success)
                    }
                    else
                        showMessage.value = appContext.getString(R.string.add_social_media_success)
                }
                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showError.value = error
                }
            })
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String?> = showMessage
    fun getShowError(): LiveData<String?> = showError
    fun getOnBackRequest(): LiveData<Void> = onBackRequest
    fun getIsOnAddEmail(): Boolean? = isAddEmail

    fun getSocialMedia(): LiveData<String> = socialMedia
}