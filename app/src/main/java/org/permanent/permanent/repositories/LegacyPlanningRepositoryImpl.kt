package org.permanent.permanent.repositories

import android.content.Context
import okhttp3.ResponseBody
import org.json.JSONObject
import org.permanent.permanent.R
import org.permanent.permanent.network.ILegacyArchiveListener
import org.permanent.permanent.network.ILegacyContactListener
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacyContact
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LegacyPlanningRepositoryImpl(val context: Context) : ILegacyPlanningRepository {

    override fun addLegacyContact(legacyContact: LegacyContact, listener: ILegacyContactListener) {
        NetworkClient.instance().addLegacyContact(legacyContact)
            .enqueue(object : Callback<LegacyContact> {

                override fun onResponse(
                    call: Call<LegacyContact>,
                    response: Response<LegacyContact>
                ) {
                    if (response.isSuccessful) {
                        val newLegacyContact = response.body()
                        if (newLegacyContact != null) {
                            listener.onSuccess(newLegacyContact)
                        } else {
                            listener.onFailed(context.getString(R.string.generic_error))
                        }
                    } else {
                        try {
                            listener.onFailed(getErrorMessage(response.errorBody()))
                        } catch (e: Exception) {
                            listener.onFailed(e.message)
                        }
                    }
                }

                override fun onFailure(call: Call<LegacyContact>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun editLegacyContact(
        legacyContactId: String,
        legacyContact: LegacyContact,
        listener: ILegacyContactListener
    ) {
        NetworkClient.instance().editLegacyContact(legacyContactId, legacyContact)
            .enqueue(object : Callback<LegacyContact> {

                override fun onResponse(
                    call: Call<LegacyContact>,
                    response: Response<LegacyContact>
                ) {
                    if (response.isSuccessful) {
                        val newLegacyContact = response.body()
                        if (newLegacyContact != null) {
                            listener.onSuccess(newLegacyContact)
                        } else {
                            listener.onFailed(context.getString(R.string.generic_error))
                        }
                    } else {
                        try {
                            listener.onFailed(getErrorMessage(response.errorBody()))
                        } catch (e: Exception) {
                            listener.onFailed(e.message)
                        }
                    }
                }

                override fun onFailure(call: Call<LegacyContact>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLegacyContact(listener: ILegacyContactsListener) {
        NetworkClient.instance().getLegacyContact().enqueue(object : Callback<List<LegacyContact>> {

            override fun onResponse(
                call: Call<List<LegacyContact>>, response: Response<List<LegacyContact>>
            ) {
                if (response.isSuccessful) {
                    val legacyContacts = response.body()
                    if (legacyContacts != null) {
                        listener.onSuccess(legacyContacts)
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(getErrorMessage(response.errorBody()))
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<List<LegacyContact>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getArchiveSteward(archiveId: Int, listener: ILegacyArchiveListener) {
        NetworkClient.instance().getArchiveSteward(archiveId = archiveId)
            .enqueue(object : Callback<List<ArchiveSteward>> {

                override fun onResponse(
                    call: Call<List<ArchiveSteward>>, response: Response<List<ArchiveSteward>>
                ) {
                    if (response.isSuccessful) {
                        val steward = response.body()
                        if (steward != null) {
                            listener.onSuccess(steward)
                        } else {
                            listener.onFailed(context.getString(R.string.generic_error))
                        }
                    } else {
                        try {
                            listener.onFailed(getErrorMessage(response.errorBody()))
                        } catch (e: Exception) {
                            listener.onFailed(e.message)
                        }
                    }
                }

                override fun onFailure(call: Call<List<ArchiveSteward>>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    private fun getErrorMessage(response: ResponseBody?): String {
        return JSONObject(response!!.string()).getJSONObject("error")
            .getJSONArray("details").getJSONObject(0).getString("message")
    }
}