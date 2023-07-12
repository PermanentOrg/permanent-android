package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Invitation
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IInvitationRepository
import org.permanent.permanent.repositories.InvitationRepositoryImpl
import org.permanent.permanent.ui.invitations.InvitationListener
import org.permanent.permanent.ui.invitations.UpdateType

class InvitationsViewModel(application: Application
) : ObservableAndroidViewModel(application), InvitationListener {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val onInvitesRetrieved = SingleLiveEvent<MutableList<Invitation>>()
    private val showSendInvitationDialog = SingleLiveEvent<Void?>()
    private var invitationRepository: IInvitationRepository = InvitationRepositoryImpl(appContext)

    init {
        refreshInvitations()
    }

    fun refreshInvitations() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        invitationRepository.getInvitations(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val invites: MutableList<Invitation> = ArrayList()

                    for (data in dataList) {
                        data.InviteVO?.let { val invitation = Invitation(it)
                            if (invitation.status == Invitation.Status.PENDING)
                                invites.add(invitation)
                        }
                    }
                    onInvitesRetrieved.value = invites
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowSnackbarMessage(): LiveData<String> = showMessage

    fun getOnInvitesRetrieved(): LiveData<MutableList<Invitation>> = onInvitesRetrieved

    fun getShowSendInvitationDialog(): LiveData<Void?> = showSendInvitationDialog

    fun onSendNewInviteBtnClick() {
        showSendInvitationDialog.call()
    }

    override fun onResendClick(invitation: Invitation) {
        invitation.inviteId?.let { updateInvite(it, UpdateType.RESEND) }
    }

    override fun onRevokeClick(invitation: Invitation) {
        invitation.inviteId?.let { updateInvite(it, UpdateType.REVOKE) }
    }

    private fun updateInvite(inviteId: Int, updateType: UpdateType) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        invitationRepository.updateInvitation(inviteId, updateType, object : IResponseListener {

            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                refreshInvitations()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }
}