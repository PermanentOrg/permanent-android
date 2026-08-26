package org.permanent.permanent.repositories

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.FeatureFlags
import org.permanent.permanent.R
import org.permanent.permanent.mapper.toRecordV2
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ArchivesV2Response
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.FolderChildrenResponse
import org.permanent.permanent.network.models.GetPresignedUrlResponse
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.UploadDestination
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Date
import java.util.Locale

class FileRepositoryImpl(val context: Context) : IFileRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)

    override fun getMyFilesRecord(listener: IRecordListener) {
        NetworkClient.instance().getRoot().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                savePublicRecordInfo(responseVO?.getPublicRecord())
                val myFilesRecord = responseVO?.getMyFilesRecord()

                if (myFilesRecord != null) {
                    listener.onSuccess(myFilesRecord)
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                            ?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    // Stela V2 replacement for the getRoot bootstrap (VSP-1788): the archives search
    // carries the archive's rootFolderId, whose children are the section roots — so
    // My Files is two V2 reads away. Any anomaly reports onFailed so the caller can
    // run the V1 getRoot failsafe. isStale short-circuits a superseded load (rapid
    // archive switch) before the second read and the prefs write.
    override fun getMyFilesRecordV2(isStale: () -> Boolean, listener: IRecordListener) {
        val currentArchiveNr = prefsHelper.getCurrentArchiveNr()
        if (currentArchiveNr.isNullOrEmpty()) {
            listener.onFailed(null)
            return
        }
        NetworkClient.instance().getArchivesV2().enqueue(object : Callback<ArchivesV2Response> {

            override fun onResponse(
                call: Call<ArchivesV2Response>,
                response: Response<ArchivesV2Response>
            ) {
                if (isStale()) {
                    listener.onFailed(null)
                    return
                }
                // The session holds archiveId as an Int, so archiveNbr is the stable
                // string-to-string key (same matching iOS ships).
                val rootFolderId = response.body()?.items
                    ?.find { it.archiveNbr == currentArchiveNr }
                    ?.rootFolderId?.toIntOrNull()?.takeIf { it > 0 }
                if (rootFolderId == null) {
                    // The message only feeds a DEBUG log in the caller; the V2 root
                    // path always falls back to V1 instead of surfacing it.
                    listener.onFailed(
                        if (BuildConfig.DEBUG) response.errorBody()?.string() else null
                    )
                    return
                }
                getMyFilesRecordFromSectionRoots(rootFolderId, isStale, listener)
            }

            override fun onFailure(call: Call<ArchivesV2Response>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    private fun getMyFilesRecordFromSectionRoots(
        rootFolderId: Int, isStale: () -> Boolean, listener: IRecordListener
    ) {
        // Reuses the children fetch so the section roots pass the same contract-failure
        // and corrupt-item rules as every listed folder.
        getChildRecordsOfV2(rootFolderId, object : IFolderChildrenListener {

            override fun onSuccess(records: List<Record>) {
                if (isStale()) {
                    listener.onFailed(null)
                    return
                }
                // Same side effect as the V1 getRoot path: these prefs are the sole
                // source for publish-to-Public and the profile banner (null fields
                // are skipped, so a missing child leaves them untouched).
                savePublicRecordInfo(
                    findSectionRoot(
                        records, Constants.PUBLIC_FILES_FOLDER_TYPE, Constants.PUBLIC_FILES_FOLDER
                    )
                )
                val myFilesRecord = findSectionRoot(
                    records, Constants.MY_FILES_FOLDER_TYPE, Constants.MY_FILES_FOLDER
                )
                if (myFilesRecord != null) {
                    listener.onSuccess(myFilesRecord)
                } else {
                    listener.onFailed(null)
                }
            }

            override fun onFailed(error: String?) {
                listener.onFailed(error)
            }
        })
    }

    // Type-first with a display-name safety net (iOS parity), and folders only — a
    // record named like a section must not be picked. Live staging sends the short
    // types "private-root"/"public-root" (captured 2026-08-20), normalized by the
    // mapper to the canonical dotted-hyphen form.
    private fun findSectionRoot(
        records: List<Record>, sectionType: String, fallbackDisplayName: String
    ): Record? {
        val folders = records.filter { it.type == RecordType.FOLDER }
        return folders.find { it.backendType == sectionType }
            ?: folders.find { it.displayName == fallbackDisplayName }
    }

    private fun savePublicRecordInfo(publicRecord: Record?) {
        prefsHelper.savePublicRecordInfo(
            publicRecord?.folderId,
            publicRecord?.folderLinkId,
            publicRecord?.archiveNr,
            publicRecord?.thumbnail256 ?: publicRecord?.thumbURL2000
        )
    }

    override fun getPublicRoot(archiveNr: String?, listener: IRecordListener) {
        NetworkClient.instance().getPublicRootForArchive(archiveNr)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    val publicRecord = responseVO?.getFolderRecord()

                    if (publicRecord != null) {
                        listener.onSuccess(publicRecord)
                    } else {
                        listener.onFailed(
                            responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                                ?.toString()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getChildRecordsOf(
        folderArchiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        navigateMin(folderArchiveNr, folderLinkId, sort, listener)
    }

    // Stela V2 replacement for the navigateMin + getLeanItems chain (VSP-1778).
    // Any anomaly reports onFailed so the caller can run the V1 failsafe: a 2xx body
    // without an items key is a contract failure, NOT an empty folder (only a present
    // and empty items array means verified empty), and an item missing a write-critical
    // id fails the whole fetch — the retained V1 writes (delete/move/rename/share) key
    // on folderLinkId + archiveNbr, so rendering such an item would break them.
    override fun getChildRecordsOfV2(
        folderId: Int,
        listener: IFolderChildrenListener
    ) {
        NetworkClient.instance().getFolderChildrenV2(folderId)
            .enqueue(object : Callback<FolderChildrenResponse> {

                override fun onResponse(
                    call: Call<FolderChildrenResponse>,
                    response: Response<FolderChildrenResponse>
                ) {
                    val items = response.body()?.items
                    if (!response.isSuccessful || items == null) {
                        listener.onFailed(
                            response.errorBody()?.string()
                                ?: context.getString(R.string.generic_error)
                        )
                        return
                    }
                    val records = items.map { it.toRecordV2() }
                    val corruptRecord = records.find { record ->
                        val itemId =
                            if (record.type == RecordType.FOLDER) record.folderId else record.recordId
                        (itemId ?: -1) <= 0 || (record.folderLinkId ?: -1) <= 0 ||
                                record.archiveNr.isNullOrEmpty()
                    }
                    if (corruptRecord != null) {
                        listener.onFailed(context.getString(R.string.generic_error))
                        return
                    }
                    listener.onSuccess(records)
                }

                override fun onFailure(call: Call<FolderChildrenResponse>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun navigateMin(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().navigateMin(archiveNr, folderLinkId)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(
                    call: Call<ResponseVO>, response: Response<ResponseVO>
                ) {
                    val folderLinkIds: MutableList<Int> = ArrayList()
                    val recordVOs: List<RecordVO?>? = response.body()?.getRecordVOs()

                    if (recordVOs != null) {
                        for (recordVO in recordVOs) {
                            recordVO?.folder_linkId?.let { folderLinkIds.add(it) }
                        }
                        getLeanItems(archiveNr, folderLinkId, sort, folderLinkIds, listener)
                    } else {
                        // A body without ChildItemVOs used to return without calling the
                        // listener, leaving callers' busy spinners stuck forever. Empty
                        // folders still send a present (empty) list, so this is anomalous.
                        listener.onFailed(
                            response.body()?.getMessages()?.firstOrNull()
                                ?: context.getString(R.string.generic_error)
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLeanItems(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        childLinkIds: List<Int>,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().getLeanItems(archiveNr, folderLinkId, sort, childLinkIds)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (response.isSuccessful && responseVO != null) {
                        listener.onSuccess(
                            responseVO.getFolderRecord()?.displayName, responseVO.getRecordVOs()
                        )
                    } else {
                        // Same terminal-and-truthful contract as navigateMin above: an
                        // error response used to report onSuccess(null, null).
                        listener.onFailed(
                            responseVO?.getMessages()?.firstOrNull()
                                ?: context.getString(R.string.generic_error)
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateProfileBanner(thumbRecord: Record, listener: IResponseListener) {
        thumbRecord.archiveNr?.let {
            NetworkClient.instance().updateProfileBanner(
                prefsHelper.getPublicRecordFolderId(),
                prefsHelper.getPublicRecordFolderLinkId(),
                prefsHelper.getPublicRecordArchiveNr(),
                it
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    prefsHelper.updatePublicRecordThumbURL(thumbRecord.thumbnail256 ?: thumbRecord.thumbURL2000)
                    listener.onSuccess("")
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }

    override fun createFolder(
        parentFolderIdentifier: NavigationFolderIdentifier, name: String, listener: IRecordListener
    ) {
        NetworkClient.instance().createFolder(
            name, parentFolderIdentifier.folderId, parentFolderIdentifier.folderLinkId
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val firstMessage = responseVO?.getMessages()?.get(0)

                if (firstMessage != null && firstMessage.startsWith("New folder")) {
                    val record = responseVO.getFolderRecord()
                    if (record != null) listener.onSuccess(record)
                } else listener.onFailed(firstMessage)
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getFolder(folderLinkId: Int, listener: IRecordListener) {
        NetworkClient.instance().getFolder(
            folderLinkId,
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val record = responseVO?.getFolderRecord()
                if (record != null) listener.onSuccess(record)
                else listener.onFailed(responseVO?.getMessages()?.get(0))
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getPresignedUrlForUpload(
        folderId: Int, folderLinkId: Int, file: File, displayName: String, mediaType: MediaType
    ): Call<GetPresignedUrlResponse> {
        return NetworkClient.instance().getPresignedUrlForUpload(
            file, displayName, folderId, folderLinkId, mediaType
        )
    }

    override fun uploadFile(
        file: File,
        mediaType: MediaType,
        uploadDestination: UploadDestination,
        listener: CountingRequestListener
    ): Call<ResponseBody> {
        return NetworkClient.instance().uploadFile(file, mediaType, uploadDestination, listener)
    }

    override fun registerRecord(
        folderId: Int,
        folderLinkId: Int,
        file: File,
        displayName: String,
        createdDT: Date,
        s3Url: String
    ): Call<ResponseVO> {
        return NetworkClient.instance().registerRecord(
            file, displayName, folderId, folderLinkId, createdDT, s3Url
        )
    }

    override fun getRecord(folderLinkId: Int, recordId: Int?): Call<ResponseVO> {
        return NetworkClient.instance().getRecord(folderLinkId, recordId)
    }

    override fun getRecord(fileArchiveNr: String): Call<ResponseVO> {
        return NetworkClient.instance().getRecord(fileArchiveNr)
    }

    override fun downloadFile(downloadUrl: String): Call<ResponseBody> {
        return NetworkClient.instance().downloadFile(downloadUrl)
    }

    override fun unshareRecord(
        record: Record, archiveId: Int, listener: IResponseListener
    ) {
        NetworkClient.instance().unshareRecord(record, archiveId)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    val isSuccessful = responseVO?.isSuccessful

                    if (isSuccessful == true) {
                        listener.onSuccess(null)
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun deleteRecords(
        records: MutableList<Record>, listener: IResponseListener
    ) {
        var areFilesReady = false
        var areFoldersReady = false
        val folders = getToRelocate(RecordType.FOLDER, records)
        val files = getToRelocate(RecordType.FILE, records)

        if (folders.isNullOrEmpty()) {
            areFoldersReady = true
        } else {
            deleteFilesOrFolders(
                folders,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFoldersReady = true
                        if (areFilesReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }

        if (files.isNullOrEmpty()) {
            areFilesReady = true
        } else {
            deleteFilesOrFolders(
                files,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFilesReady = true
                        if (areFoldersReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }
    }

    private fun deleteFilesOrFolders(records: MutableList<Record>, listener: IResponseListener) {
        NetworkClient.instance().deleteFilesOrFolders(records).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(null)
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun relocateRecords(
        records: MutableList<Record>,
        destFolderLinkId: Int,
        destFolderId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    ) {
        // PUBLISH is a copy into the public workspace, so it routes like COPY.
        val isCopyLike =
            relocationType == ModificationType.COPY || relocationType == ModificationType.PUBLISH
        if (FeatureFlags.useStelaMigration && isCopyLike && destFolderId > 0) {
            val sessionArchiveId = prefsHelper.getCurrentArchiveId()
            val (v2Records, v1Rest) = records.partition { it.isEligibleForStelaCopy(sessionArchiveId) }
            if (v2Records.isNotEmpty()) {
                copyViaStelaV2(
                    v2Records, v1Rest.toMutableList(), destFolderLinkId, destFolderId,
                    relocationType, listener
                )
                return
            }
        }
        relocateRecordsV1(records, destFolderLinkId, relocationType, listener)
    }

    private val Record.stelaRecordId: Int
        get() = recordId ?: id ?: 0

    // Cross-archive copies need Owner on the origin archive — foreign items stay on V1.
    private fun Record.isEligibleForStelaCopy(sessionArchiveId: Int): Boolean =
        type == RecordType.FILE && stelaRecordId > 0 && archiveId == sessionArchiveId

    // No V1 failsafe and no retry — an ambiguous failure retried would duplicate
    // the copy. Serial, best-effort; the V1 remainder follows the V2 items.
    private fun copyViaStelaV2(
        v2Records: List<Record>,
        v1Rest: MutableList<Record>,
        destFolderLinkId: Int,
        destFolderId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    ) {
        fun finish(hadFailure: Boolean) {
            fun report(message: String?) =
                if (hadFailure) listener.onFailed(context.getString(R.string.generic_error))
                else listener.onSuccess(message)

            if (v1Rest.isEmpty()) {
                report(relocationSuccessMessage(v2Records[0], relocationType))
            } else {
                relocateRecordsV1(v1Rest, destFolderLinkId, relocationType,
                    object : IResponseListener {
                        override fun onSuccess(message: String?) = report(message)
                        override fun onFailed(error: String?) = listener.onFailed(error)
                    })
            }
        }

        fun copyNext(index: Int, hadFailure: Boolean) {
            if (index == v2Records.size) {
                finish(hadFailure)
                return
            }
            NetworkClient.instance().copyRecordV2(v2Records[index].stelaRecordId, destFolderId)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        copyNext(index + 1, hadFailure || !response.isSuccessful)
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        copyNext(index + 1, true)
                    }
                })
        }
        copyNext(0, false)
    }

    private fun relocateRecordsV1(
        records: MutableList<Record>,
        destFolderLinkId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    ) {
        var areFilesReady = false
        var areFoldersReady = false
        val folders = getToRelocate(RecordType.FOLDER, records)
        val files = getToRelocate(RecordType.FILE, records)

        if (folders.isNullOrEmpty()) {
            areFoldersReady = true
        } else {
            relocateFilesOrFolders(
                folders,
                destFolderLinkId,
                relocationType,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFoldersReady = true
                        if (areFilesReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }

        if (files.isNullOrEmpty()) {
            areFilesReady = true
        } else {
            relocateFilesOrFolders(
                files,
                destFolderLinkId,
                relocationType,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFilesReady = true
                        if (areFoldersReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }
    }

    override fun updateRecords(fileDataList: List<FileData?>, listener: IResponseListener) {
        NetworkClient.instance().updateRecords(fileDataList).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.file_info_update_success))
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateMultipleRecords(
        records: MutableList<Record>,
        locnVO: LocnVO,
        listener: IResponseListener
    ) {
        NetworkClient.instance().updateMultipleRecords(records = records, locnVO = locnVO).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.file_info_update_success))
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateMultipleRecords(records: MutableList<Record>,
                                       isFolderRecordType: Boolean,
                                       listener: IResponseListener) {
        NetworkClient.instance().updateMultipleRecords(records = records, isFolderRecordType).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.file_info_update_success))
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    private fun relocationSuccessMessage(
        record: Record, relocationType: ModificationType
    ): String {
        val relocationVerb = when (relocationType) {
            ModificationType.MOVE -> context.getString(R.string.relocation_type_moved)
            ModificationType.PUBLISH -> context.getString(R.string.relocation_type_published)
            else -> context.getString(R.string.relocation_type_copied)
        }
        return context.getString(
            R.string.relocation_success,
            record.type?.toTitleCase(),
            relocationVerb
        )
    }

    private fun getToRelocate(
        recordType: RecordType, records: MutableList<Record>
    ): MutableList<Record> {
        return records.filter { it.type == recordType }.toMutableList()
    }

    private fun relocateFilesOrFolders(
        recordsToRelocate: MutableList<Record>,
        destFolderLinkId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    ) {
        NetworkClient.instance().relocateFilesOrFolders(
            recordsToRelocate, destFolderLinkId, relocationType
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(
                        relocationSuccessMessage(recordsToRelocate[0], relocationType)
                    )
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateRecord(locnVO: LocnVO, fileData: FileData, listener: IResponseListener) {
        NetworkClient.instance().updateRecord(locnVO, fileData)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.file_location_update_success))
                    } else {
                        listener.onFailed(context.getString(R.string.file_location_update_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateRecord(record: Record, newName: String, listener: IResponseListener) {
        NetworkClient.instance().updateRecord(record, newName)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.rename_record_rename_success))
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun searchRecords(
        query: String?, tags: List<Tag>, listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().searchRecords(query, tags).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(
                        null, responseVO.getData()?.get(0)?.SearchVO?.ChildItemVOs
                    )
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}