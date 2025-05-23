package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArchiveRepositoryImpl(val context: Context) : IArchiveRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    override fun getArchivesByNr(archiveNrs: List<String?>, listener: IDataListener) {
        NetworkClient.instance().getArchivesByNr(archiveNrs).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getDataFromResults())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun searchArchive(name: String?, listener: IDataListener) {
        NetworkClient.instance().searchArchive(name).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(responseVO.getData())
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateProfilePhoto(thumbRecord: Record, listener: IResponseListener) {
        NetworkClient.instance().updateProfilePhoto(
            prefsHelper.getCurrentArchiveNr(),
            prefsHelper.getCurrentArchiveId(),
            prefsHelper.getCurrentArchiveType(),
            thumbRecord.archiveNr
        ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        prefsHelper.updateCurrentArchiveThumbURL(thumbRecord.thumbURL2000)
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getAllArchives(listener: IDataListener) {
        NetworkClient.instance().getAllArchives().enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getData())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun acceptArchives(archives: List<Archive>, listener: IResponseListener) {
        NetworkClient.instance().acceptArchives(archives).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.archive_accept_pending_archive_success))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun declineArchive(archive: Archive, listener: IResponseListener) {
        NetworkClient.instance().declineArchive(archive).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.archive_decline_pending_archive_success))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun switchToArchive(archiveNr: String, listener: IDataListener) {
        NetworkClient.instance().switchToArchive(archiveNr).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getData())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun createNewArchive(
        name: String, type: ArchiveType, listener: IArchiveRepository.IArchiveListener
    ) {
        NetworkClient.instance().createNewArchive(name, type)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(Archive(responseVO.getArchiveVO()))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun deleteArchive(archiveNr: String, listener: IResponseListener) {
        NetworkClient.instance().deleteArchive(archiveNr).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.archive_delete_archive_success))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getMembers(listener: IDataListener) {
        NetworkClient.instance().getMembers(prefsHelper.getCurrentArchiveNr())
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getData())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun addMember(email: String, accessRole: AccessRole, listener: IResponseListener) {
        NetworkClient.instance().addMember(
            prefsHelper.getCurrentArchiveNr(), email, accessRole
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.members_member_added_successfully))
                } else if (responseVO?.getMessages()?.get(0) == Constants.ERROR_MEMBER_ALREADY_ADDED
                ) {
                    listener.onFailed(context.getString(R.string.members_member_already_added))
                } else if (responseVO?.getMessages()?.get(0) == Constants.ERROR_ARCHIVE_NO_EMAIL_FOUND
                ) {
                    listener.onFailed(context.getString(R.string.no_account_found))
                } else listener.onFailed(responseVO?.getMessages()?.get(0))
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateMember(
        accountId: Int, email: String, accessRole: AccessRole, listener: IResponseListener
    ) {
        NetworkClient.instance().updateMember(
            prefsHelper.getCurrentArchiveNr(), accountId, email, accessRole
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.members_member_updated_successfully))
                } else if (responseVO?.getMessages()
                        ?.get(0) == Constants.ERROR_PENDING_OWNER_NOT_EDITABLE
                ) {
                    listener.onFailed(context.getString(R.string.members_pending_owner_not_editable))
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun transferOwnership(email: String, listener: IResponseListener) {
        NetworkClient.instance().transferOwnership(
            prefsHelper.getCurrentArchiveNr(), email
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.members_ownership_transfer_request_sent_successfully))
                } else if (responseVO?.getMessages()
                        ?.get(0) == Constants.ERROR_OWNER_ALREADY_PENDING
                ) {
                    listener.onFailed(context.getString(R.string.members_owner_already_pending))
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun deleteMember(accountId: Int, email: String, listener: IResponseListener) {
        NetworkClient.instance().deleteMember(
            prefsHelper.getCurrentArchiveNr(), accountId, email
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.members_member_removed_successfully))
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}