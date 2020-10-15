package org.permanent.permanent.repositories

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FileRepositoryImpl(application: Application): IFileRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun getMyFilesRecord(listener: IFileRepository.IOnMyFilesArchiveNrListener) {
        networkClient.getRoot(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                val myFilesRecord = responseVO?.getMyFilesRecordVO()

                if (myFilesRecord != null) {
                    listener.onSuccess(myFilesRecord)
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getChildRecordsOf(myFilesArchiveNr: String,
                                   listener: IFileRepository.IOnRecordsRetrievedListener) {
        navigateMin(myFilesArchiveNr, listener)
    }

    override fun navigateMin(archiveNumber: String,
                             listener: IFileRepository.IOnRecordsRetrievedListener) {
        networkClient.navigateMin(prefsHelper.getCsrf(), archiveNumber)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(
                    call: Call<ResponseVO>,
                    response: Response<ResponseVO>
                ) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    val folderLinkIds: MutableList<Int> = ArrayList()
                    val childItemVOs: List<RecordVO?>? = response.body()?.getChildItemVOs()

                    if (childItemVOs != null) {
                        for (recordVO in childItemVOs) {
                            recordVO?.folder_linkId?.let { folderLinkIds.add(it) }
                        }
                        getLeanItems(archiveNumber, folderLinkIds, listener)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLeanItems(archiveNumber: String, childLinks: List<Int>,
                              listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.getLeanItems(prefsHelper.getCsrf(), archiveNumber, childLinks)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    listener.onSuccess(responseVO?.getRecordVOs())
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }
}