package org.permanent.permanent.repositories

import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileRepositoryImpl : IProfileRepository {

    override fun getProfileItemsByArchive(archiveNr: String?, listener: IDataListener) {
        NetworkClient.instance()
            .getProfileItemsByArchive(archiveNr)
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

    override fun safeAddUpdateProfileItems(
        profileItems: List<ProfileItem>,
        serializeNulls: Boolean,
        listener: IProfileItemListener
    ) {
        NetworkClient.instance()
            .safeAddUpdateProfileItems(profileItems, serializeNulls)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(ProfileItem(responseVO.getProfileItemVO(), false))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun deleteProfileItem(profileItem: ProfileItem, listener: IProfileItemListener) {
        NetworkClient.instance()
            .deleteProfileItem(profileItem)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(ProfileItem(responseVO.getProfileItemVO(), false))
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