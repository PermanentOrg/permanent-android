package org.permanent.permanent.viewmodels

import android.app.Application
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl

class AccessRolesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var shareByUrlVO: Shareby_urlVO? = null
    private var share: Share? = null
    private val checkedAccessRole = MutableLiveData(AccessRole.VIEWER)

    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showAccessRolesDocumentation = SingleLiveEvent<Void?>()
    private val onCloseScreenRequest = SingleLiveEvent<Void?>()
    private val onAccessRoleUpdated = SingleLiveEvent<AccessRole?>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setShareLink(shareByUrlVO: Shareby_urlVO?) {
        this.shareByUrlVO = shareByUrlVO
        shareByUrlVO?.let {
            checkedAccessRole.value = AccessRole.createFromBackendString(it.defaultAccessRole)
        }
    }

    fun setShare(share: Share?) {
        this.share = share
        share?.let {
            checkedAccessRole.value = it.accessRole
        }
    }

    fun onAccessRoleChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.rbViewer -> checkedAccessRole.value = AccessRole.VIEWER
            R.id.rbContributor -> checkedAccessRole.value = AccessRole.CONTRIBUTOR
            R.id.rbEditor -> checkedAccessRole.value = AccessRole.EDITOR
            R.id.rbCurator -> checkedAccessRole.value = AccessRole.CURATOR
            R.id.rbOwner -> checkedAccessRole.value = AccessRole.OWNER
            R.id.rbRemoveFromShare -> checkedAccessRole.value = null
        }
    }

    fun onWhatsThisBtnClick() {
        showAccessRolesDocumentation.call()
    }

    fun onCancelBtnClick() {
        onCloseScreenRequest.call()
    }

    fun onUpdateRoleBtnClick() {
        if (shareByUrlVO != null) updateLinkAccessRole()
        else if (checkedAccessRole.value == null) removeShare()
        else updateShareAccessRole()
    }

    private fun updateLinkAccessRole() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        shareByUrlVO?.let { shareByUrlVo ->
            shareByUrlVo.defaultAccessRole = checkedAccessRole.value?.backendString

            isBusy.value = true
            shareRepository.modifyShareLink(shareByUrlVo, ShareRequestType.UPDATE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onAccessRoleUpdated.value = checkedAccessRole.value
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showSnackbar.value = it }
                    }
                })
        }
    }

    private fun updateShareAccessRole() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        share?.let {
            it.accessRole = checkedAccessRole.value

            isBusy.value = true
            shareRepository.updateShare(it, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    onAccessRoleUpdated.value = checkedAccessRole.value
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMsg -> showSnackbar.value = errorMsg }
                }
            })
        }
    }

    private fun removeShare() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        share?.let { share ->
            isBusy.value = true
            shareRepository.deleteShare(share, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    message?.let { showSnackbarSuccess.value = it }
                    onAccessRoleUpdated.value = null
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showSnackbar.value = it }
                }
            })
        }
    }

    fun getShareByUrlVO(): Shareby_urlVO? = shareByUrlVO

    fun getShare(): Share? = share

    fun getCheckedAccessRole(): MutableLiveData<AccessRole> = checkedAccessRole

    fun getOnAccessRoleUpdated(): MutableLiveData<AccessRole?> = onAccessRoleUpdated

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnCloseScreenRequest(): SingleLiveEvent<Void?> = onCloseScreenRequest

    fun getShowAccessRolesDocumentation(): SingleLiveEvent<Void?> = showAccessRolesDocumentation
}