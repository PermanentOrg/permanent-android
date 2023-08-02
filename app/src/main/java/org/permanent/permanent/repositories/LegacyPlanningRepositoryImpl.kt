package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.ILegacyAccountListener
import org.permanent.permanent.network.ILegacyArchiveListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacySteward
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LegacyPlanningRepositoryImpl(val context: Context) : ILegacyPlanningRepository {

    override fun getLegacyContact(listener: ILegacyAccountListener) {
        NetworkClient.instance().getLegacyContact().enqueue(object : Callback<List<LegacySteward>> {

            override fun onResponse(
                call: Call<List<LegacySteward>>, response: Response<List<LegacySteward>>
            ) {
                val steward = response?.body()
                if (steward != null) {
                    listener.onSuccess(steward)
                } else {
                    listener.onFailed("Error")
                }
            }

            override fun onFailure(call: Call<List<LegacySteward>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getArchiveSteward(archiveInt: Int, listener: ILegacyArchiveListener) {
        NetworkClient.instance().getArchiveSteward(archiveId = archiveInt).enqueue(object : Callback<List<ArchiveSteward>> {

            override fun onResponse(
                call: Call<List<ArchiveSteward>>, response: Response<List<ArchiveSteward>>
            ) {
                val steward = response?.body()
                if (steward != null) {
                    listener.onSuccess(steward)
                } else {
                    listener.onFailed("Error")
                }
            }

            override fun onFailure(call: Call<List<ArchiveSteward>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}