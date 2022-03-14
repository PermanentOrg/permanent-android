package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class MilestoneListViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()
    private var existsMilestones = MutableLiveData(false)
    private var milestoneProfileItems: MutableList<ProfileItem> = ArrayList()
    private var onMilestonesRetrieved = MutableLiveData<List<ProfileItem>>()
    private var addMilestoneRequest = SingleLiveEvent<Void>()
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl(application)

    fun getProfileItems() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        profileRepository.getProfileItemsByArchive(
            prefsHelper.getCurrentArchiveNr(),
            object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    dataList?.let { displayMilestones(dataList) }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            }
        )
    }

    fun displayMilestones(dataList: List<Datum>) {
        milestoneProfileItems = ArrayList()
        for (datum in dataList) {
            val profileItem = ProfileItem(datum.Profile_itemVO, false)
            if (profileItem.fieldName == ProfileItemName.MILESTONE) {
                milestoneProfileItems.add(profileItem)
            }
        }
        if (milestoneProfileItems.isEmpty()) {
            existsMilestones.value = false
        } else {
            existsMilestones.value = true
            onMilestonesRetrieved.value = milestoneProfileItems
        }
    }

    fun deleteProfileItem(profileItem: ProfileItem) {
        isBusy.value = true
        profileRepository.deleteProfileItem(profileItem, object : IProfileItemListener {
            override fun onSuccess(profileItem: ProfileItem) {
                isBusy.value = false
                getProfileItems()
                showMessage.value = appContext.getString(R.string.milestone_delete_success)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { showError.value = it }
            }
        })
    }

    fun onAddMilestoneBtnClick() {
        addMilestoneRequest.call()
    }

    fun getExistsMilestones(): MutableLiveData<Boolean> = existsMilestones

    fun getOnMilestonesRetrieved(): LiveData<List<ProfileItem>> = onMilestonesRetrieved

    fun getOnAddMilestoneRequest(): LiveData<Void> = addMilestoneRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getShowError(): LiveData<String> = showError
}
