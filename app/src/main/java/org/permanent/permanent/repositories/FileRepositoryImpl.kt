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

    override fun getRecordVOs(listener: IFileRepository.IOnRecordsRetrievedListener) {
        getRoot(listener)
    }

    override fun getRoot(listener: IFileRepository.IOnRecordsRetrievedListener) {
        networkClient.getRoot(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val csrf = responseVO?.csrf
                prefsHelper.saveCsrf(csrf)
                val archiveNumber = responseVO?.getMyFilesRecordVO()?.archiveNbr

                if (archiveNumber != null) {
                    navigateMin(csrf, archiveNumber, listener)
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

    override fun navigateMin(
        csrf: String?,
        archiveNumber: String,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.navigateMin(csrf, archiveNumber).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(
                call: Call<ResponseVO>,
                response: Response<ResponseVO>
            ) {
                val responseVO = response.body()
                val responseCsrf = responseVO?.csrf
                prefsHelper.saveCsrf(responseCsrf)
                val folderLinkIds: MutableList<Int> = ArrayList()
                val childItemVOs: List<RecordVO?>? = response.body()?.getChildItemVOs()

                if (childItemVOs != null) {
                    for (recordVO in childItemVOs) {
                        recordVO?.folder_linkId?.let { folderLinkIds.add(it) }
                    }
                    getLeanItems(responseCsrf, archiveNumber, folderLinkIds, listener)
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getLeanItems(
        csrf: String?, archiveNumber: String, childLinks: List<Int>,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.getLeanItems(csrf, archiveNumber, childLinks).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(
                call: Call<ResponseVO>,
                response: Response<ResponseVO>
            ) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)

//                val folderVO: FolderVO = responseVO.getFolderVO()
//                mFolderName = folderVO.displayName
//                mFolderLinkId = folderVO.folder_linkId
//                mFolderId = folderVO.folderId
//                consumer.accept(responseVO?.getThumbnails())

                listener.onSuccess(responseVO?.getRecordVOs())
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}