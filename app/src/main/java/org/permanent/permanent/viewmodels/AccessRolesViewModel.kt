package org.permanent.permanent.viewmodels

import android.app.Application
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl

class AccessRolesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var shareByUrlVO: Shareby_urlVO? = null
    private val checkedAccessRole = MutableLiveData<AccessRole>()

    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onCloseSheetRequest = SingleLiveEvent<Void>()
    private val onAccessRoleUpdated = SingleLiveEvent<AccessRole>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setShareLink(shareByUrlVO: Shareby_urlVO?) {
        this.shareByUrlVO = shareByUrlVO
        checkedAccessRole.value =
            AccessRole.createFromBackendString(shareByUrlVO?.defaultAccessRole)
    }

    fun onAccessRoleChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.rbViewer -> checkedAccessRole.value = AccessRole.VIEWER
            R.id.rbContributor -> checkedAccessRole.value = AccessRole.CONTRIBUTOR
            R.id.rbEditor -> checkedAccessRole.value = AccessRole.EDITOR
            R.id.rbCurator -> checkedAccessRole.value = AccessRole.CURATOR
            R.id.rbManager -> checkedAccessRole.value = AccessRole.MANAGER
            R.id.rbOwner -> checkedAccessRole.value = AccessRole.OWNER
            R.id.rbRemoveFromShare -> checkedAccessRole.value = null
        }
    }

    fun onCancelBtnClick() {
        onCloseSheetRequest.call()
    }

    fun onUpdateRoleBtnClick() {
        if (shareByUrlVO != null) updateLinkAccessRole()
    }

    private fun updateLinkAccessRole() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        shareByUrlVO?.let {
            it.defaultAccessRole = checkedAccessRole.value?.backendString

            isBusy.value = true
            shareRepository.modifyShareLink(it, ShareRequestType.UPDATE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onAccessRoleUpdated.value = checkedAccessRole.value
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    fun getShareByUrlVO(): Shareby_urlVO? = shareByUrlVO

    fun getCheckedAccessRole(): MutableLiveData<AccessRole> = checkedAccessRole

    fun getOnAccessRoleUpdated(): MutableLiveData<AccessRole> = onAccessRoleUpdated

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnCloseSheetRequest(): LiveData<Void> = onCloseSheetRequest
}