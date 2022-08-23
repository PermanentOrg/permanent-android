package org.permanent.permanent.network

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.*
import org.permanent.permanent.network.models.*
import org.permanent.permanent.ui.invitations.UpdateType
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestBody
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.*


class NetworkClient(private var okHttpClient: OkHttpClient?, context: Context) {
    private val baseUrl: String = BuildConfig.BASE_API_URL
    private val retrofit: Retrofit
    private val authService: IAuthService
    private val accountService: IAccountService
    private val fileService: IFileService
    private val shareService: IShareService
    private val archiveService: IArchiveService
    private val notificationService: INotificationService
    private val invitationService: IInvitationService
    private val locationService: ILocationService
    private val tagService: ITagService
    private val profileService: IProfileService
    private val storageService: IStorageService
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val simpleJsonAdapter: JsonAdapter<SimpleRequestContainer>
    private val profileItemsJsonAdapter: JsonAdapter<ProfileItemsRequestContainer>
    private val jsonMediaType: MediaType = Constants.MEDIA_TYPE_JSON.toMediaType()

    companion object {
        private var instance: NetworkClient? = null

        fun instance(): NetworkClient {
            if (instance == null) {
                instance = NetworkClient(null, PermanentApplication.instance.applicationContext)
            }
            return instance!!
        }
    }

    init {
        if (okHttpClient == null) {
            val cookieJar: ClearableCookieJar = PersistentCookieJar(
                SetCookieCache(),
                SharedPrefsCookiePersistor(context)
            )

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE

            okHttpClient = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(UnauthorizedInterceptor())
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    if (!request.url.toString().contains(Constants.S3_BASE_URL) &&
                        !request.url.toString().contains(Constants.SIGN_UP_URL_SUFFIX) &&
                        !request.url.toString().contains(Constants.STRIPE_URL)
                    ) {
                        val requestBuilder: Request.Builder = request.newBuilder()
                        requestBuilder.header(
                            "Authorization",
                            "Bearer ${AuthStateManager.getInstance(context).current.accessToken}"
                        )
                        chain.proceed(requestBuilder.build())
                    } else chain.proceed(request)
                })
//                .authenticator(TokenAuthenticator())
                .build()
        }
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()

        authService = retrofit.create(IAuthService::class.java)
        accountService = retrofit.create(IAccountService::class.java)
        fileService = retrofit.create(IFileService::class.java)
        shareService = retrofit.create(IShareService::class.java)
        archiveService = retrofit.create(IArchiveService::class.java)
        notificationService = retrofit.create(INotificationService::class.java)
        invitationService = retrofit.create(IInvitationService::class.java)
        locationService = retrofit.create(ILocationService::class.java)
        tagService = retrofit.create(ITagService::class.java)
        profileService = retrofit.create(IProfileService::class.java)
        storageService = retrofit.create(IStorageService::class.java)
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
        simpleJsonAdapter = Moshi.Builder().build().adapter(SimpleRequestContainer::class.java)
        profileItemsJsonAdapter =
            Moshi.Builder().build().adapter(ProfileItemsRequestContainer::class.java)
    }

    fun verifyLoggedIn(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return authService.verifyLoggedIn(requestBody)
    }

    fun login(email: String, password: String): Call<ResponseVO> {
        val request: String = toJson(
            RequestContainer().addAccount(email).addAccountPassword(password)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.login(requestBody)
    }

    fun logout(): Call<ResponseVO> {
        val request: String = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.logout(requestBody)
    }

    fun forgotPassword(email: String): Call<ResponseVO> {
        val request: String = toJson(RequestContainer().addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.forgotPassword(requestBody)
    }

    fun sendSMSVerificationCode(accountId: Int, email: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addAccount(accountId, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.sendSMSVerificationCode(requestBody)
    }

    fun verifyCode(
        code: String, authType: String, email: String
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer().addAuth(code, authType).addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.verifyCode(requestBody)
    }

    fun signUp(fullName: String, email: String, password: String): Call<ResponseVO> {
        val request = toJson(
            RequestContainer()
                .addAccountPassword(password, password)
                .addAccount(fullName, email, optIn = false, agreed = true)
                .addSimple("createArchive", false)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.signUp(requestBody)
    }

    fun getAccount(accountId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addAccount(accountId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.getAccount(requestBody)
    }

    fun getSessionAccount(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return accountService.getSessionAccount(requestBody)
    }

    fun updateAccount(account: Account): Call<ResponseVO> {
        val request = toJson(RequestContainer().addAccount(account))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.updateAccount(requestBody)
    }

    fun changeDefaultArchive(
        accountId: Int,
        accountEmail: String,
        defaultArchiveId: Int
    ): Call<ResponseVO> {
        val request =
            toJson(RequestContainer().addAccount(accountId, accountEmail, defaultArchiveId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.updateAccount(requestBody)
    }

    fun deleteAccount(accountId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addAccount(accountId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.deleteAccount(requestBody)
    }

    fun changePassword(
        accountId: Int, currentPassword: String, newPassword: String, retypedPassword: String
    ): Call<ResponseVO> {
        val request = toJson(
            RequestContainer()
                .addAccount(accountId)
                .addAccountPassword(currentPassword, newPassword, retypedPassword)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.changePassword(requestBody)
    }

    fun getRoot(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRoot(requestBody)
    }

    fun getPublicRootForArchive(archiveNr: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getPublicRoot(requestBody)
    }

    fun navigateMin(archiveNr: String, folderLinkId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addFolder(archiveNr, folderLinkId, null))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.navigateMin(requestBody)
    }

    fun getLeanItems(
        archiveNumber: String,
        folderLinkId: Int,
        sort: String?,
        childItems: List<Int>
    ): Call<ResponseVO> {
        val request = toJson(
            RequestContainer()
                .addFolder(archiveNumber, folderLinkId, sort, childItems)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getLeanItems(requestBody)
    }

    fun updateProfileBanner(
        folderId: Int, folderLinkId: Int, archiveNr: String?, thumbArchiveNr: String
    ): Call<ResponseVO> {
        val json = toJson(
            RequestContainer().addFolder(folderId, folderLinkId, archiveNr, thumbArchiveNr)
        )
        val requestBody: RequestBody = json.toRequestBody(jsonMediaType)

        return fileService.updateProfileBanner(requestBody)
    }

    fun createFolder(
        name: String, folderId: Int, folderLinkId: Int
    ): Call<ResponseVO> {
        val json = toJson(RequestContainer().addFolder(name, folderId, folderLinkId))
        val requestBody: RequestBody = json.toRequestBody(jsonMediaType)

        return fileService.createFolder(requestBody)
    }

    fun getFolder(folderLinkId: Int): Call<ResponseVO> {
        val json = toJson(RequestContainer().addFolder(folderLinkId))
        val requestBody: RequestBody = json.toRequestBody(jsonMediaType)

        return fileService.getFolder(requestBody)
    }

    fun getPresignedUrlForUpload(
        file: File,
        displayName: String,
        folderId: Int,
        folderLinkId: Int,
        mediaType: MediaType
    ): Call<GetPresignedUrlResponse> {
        val request = toJson(
            RequestContainer()
                .addRecord(displayName, file, folderId, folderLinkId)
                .addSimple("type", mediaType.type + "/" + mediaType.subtype)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getPresignedUrl(requestBody)
    }

    fun uploadFile(
        file: File,
        mediaType: MediaType,
        uploadDestination: UploadDestination,
        listener: CountingRequestListener
    ): Call<ResponseBody> {
        val url = uploadDestination.presignedPost?.url
        val fields: Map<String, RequestBody>? =
            uploadDestination.presignedPost?.getFieldsMapForCall()
        val contentType = (mediaType.toString()).toRequestBody(MultipartBody.FORM)
        val fileRequestBody = CountingRequestBody(file.asRequestBody(mediaType), listener)
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file", file.name, fileRequestBody
        )

        return fileService.upload(url!!, fields!!, contentType, body)
    }

    fun registerRecord(
        file: File,
        displayName: String,
        folderId: Int,
        folderLinkId: Int,
        createdDT: Date,
        s3Url: String
    ): Call<ResponseVO> {
        val request = toJson(
            SimpleRequestContainer()
                .addRecord(displayName, file, folderId, folderLinkId, createdDT)
                .addSimple(s3Url)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.registerRecord(requestBody)
    }

    fun getRecord(
        folderLinkId: Int?,
        recordId: Int?
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(folderLinkId, recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRecord(requestBody)
    }

    fun downloadFile(url: String): Call<ResponseBody> {
        return fileService.download(url)
    }

    fun deleteRecord(record: Record): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(record))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (record.type == RecordType.FOLDER) {
            fileService.deleteFolder(requestBody)
        } else {
            fileService.deleteRecord(requestBody)
        }
    }

    fun relocateRecord(
        recordToRelocate: Record,
        destFolderLinkId: Int,
        relocationType: RelocationType
    ): Call<ResponseVO> {
        val request = toJson(
            RequestContainer().addRecord(recordToRelocate).addFolderDest(destFolderLinkId)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (recordToRelocate.type == RecordType.FOLDER) {
            if (relocationType == RelocationType.MOVE) {
                fileService.moveFolder(requestBody)
            } else {
                fileService.copyFolder(requestBody)
            }
        } else {
            if (relocationType == RelocationType.MOVE) {
                fileService.moveRecord(requestBody)
            } else {
                fileService.copyRecord(requestBody)
            }
        }
    }

    fun updateRecord(fileData: FileData): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(fileData))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return fileService.updateRecord(requestBody)
    }

    fun updateRecord(locnVO: LocnVO, fileData: FileData): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(locnVO, fileData))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return fileService.updateRecord(requestBody)
    }

    fun updateRecord(record: Record, newName: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(record, newName))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (record.type == RecordType.FOLDER) {
            fileService.updateFolder(requestBody)
        } else {
            fileService.updateRecord(requestBody)
        }
    }

    fun searchRecords(query: String?, tags: List<Tag>): Call<ResponseVO> {
        val request = toJson(RequestContainer().addSearch(query, tags))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return fileService.searchRecord(requestBody)
    }

    fun requestShareLink(
        record: Record, shareRequestType: ShareRequestType
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer().addRecord(record))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.GET) {
            shareService.getShareLink(requestBody)
        } else {
            shareService.generateShareLink(requestBody)
        }
    }

    fun modifyShareLink(
        shareVO: Shareby_urlVO, shareRequestType: ShareRequestType
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer().addShareByUrl(shareVO))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.DELETE) {
            shareService.deleteShareLink(requestBody)
        } else {
            shareService.updateShareLink(requestBody)
        }
    }

    // Used for both approve and update access role
    fun updateShare(share: Share): Call<ResponseVO> {
        val request = toJson(RequestContainer().addShare(share))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.updateShare(requestBody)
    }

    // Used for both deny and remove
    fun deleteShare(share: Share): Call<ResponseVO> {
        val request = toJson(RequestContainer().addShare(share))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.deleteShare(requestBody)
    }

    fun checkShareLink(urlToken: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addShareByUrl(urlToken))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.checkShareLink(requestBody)
    }

    fun requestShareAccess(urlToken: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addShareByUrl(urlToken))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.requestShareAccess(requestBody)
    }

    fun getShares(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.getShares(requestBody)
    }

    fun getArchivesByNr(archiveNrs: List<String?>): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchiveNrs(archiveNrs))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.getArchivesByNr(requestBody)
    }

    fun searchArchive(name: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer().addSearch(name))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.searchArchive(requestBody)
    }

    fun updateProfilePhoto(
        archiveNr: String?,
        archiveId: Int,
        thumbArchiveNr: String?
    ): Call<ResponseVO> {
        val request =
            toJson(RequestContainer().addArchive(archiveNr, archiveId, thumbArchiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.updateProfilePhoto(requestBody)
    }

    fun getAllArchives(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.getAllArchives(requestBody)
    }

    fun acceptArchives(archive: List<Archive>): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchives(archive))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.acceptArchive(requestBody)
    }

    fun declineArchive(archive: Archive): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archive))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.declineArchive(requestBody)
    }

    fun switchToArchive(archiveNr: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.switchArchive(requestBody)
    }

    fun createNewArchive(name: String, type: ArchiveType): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(name, type))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.createNewArchive(requestBody)
    }

    fun deleteArchive(archiveNr: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.deleteArchive(requestBody)
    }

    fun getMembers(archiveNr: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.getMembers(requestBody)
    }

    fun addMember(
        archiveNr: String?,
        email: String,
        accessRole: AccessRole
    ): Call<ResponseVO> {
        val request = toJson(
            RequestContainer()
                .addArchive(archiveNr)
                .addAccount(email, accessRole)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.addMember(requestBody)
    }

    fun updateMember(
        archiveNr: String?,
        id: Int,
        email: String,
        accessRole: AccessRole
    ): Call<ResponseVO> {
        val request =
            toJson(RequestContainer().addArchive(archiveNr).addAccount(id, email, accessRole))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.updateMember(requestBody)
    }

    fun transferOwnership(archiveNr: String?, email: String): Call<ResponseVO> {
        val request = toJson(
            RequestContainer()
                .addArchive(archiveNr)
                .addAccount(email, AccessRole.OWNER)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.transferOwnership(requestBody)
    }

    fun deleteMember(archiveNr: String?, id: Int, email: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveNr).addAccount(id, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return archiveService.deleteMember(requestBody)
    }

    fun getNotifications(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return notificationService.getNotifications(requestBody)
    }

    fun registerDevice(token: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addSimple("token", token))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return notificationService.registerDevice(requestBody)
    }

    fun deleteDeviceToken(token: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addSimple("token", token))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return notificationService.deleteToken(requestBody)
    }

    fun getInvitations(): Call<ResponseVO> {
        val request = toJson(RequestContainer())
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return invitationService.getInvitations(requestBody)
    }

    fun sendInvitation(fullName: String, email: String): Call<ResponseVO> {
        val request = toJson(RequestContainer().addInvite(fullName, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return invitationService.sendInvitation(requestBody)
    }

    fun updateInvitation(inviteId: Int, updateType: UpdateType): Call<ResponseVO> {
        val request = toJson(RequestContainer().addInvite(inviteId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (updateType == UpdateType.REVOKE) {
            invitationService.revokeInvitation(requestBody)
        } else {
            invitationService.resendInvitation(requestBody)
        }
    }

    fun getLocation(latLng: LatLng): Call<ResponseVO> {
        val request = toJson(RequestContainer().addLocation(latLng))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return locationService.getLocation(requestBody)
    }

    fun getTagsByArchive(archiveId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addArchive(archiveId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.getTagsByArchive(requestBody)
    }

    fun createOrLinkTag(tags: List<Tag>, recordId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addTagNames(tags).addTagLink(recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.createOrLinkTags(requestBody)
    }

    fun unlinkTags(tags: List<Tag>, recordId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer().addTagIds(tags).addTagLink(recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.unlinkTags(requestBody)
    }

    fun getProfileItemsByArchive(archiveNr: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer().addProfileItem(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return profileService.getAllByArchiveNbr(requestBody)
    }

    fun safeAddUpdateProfileItems(
        profileItems: List<ProfileItem>,
        serializeNulls: Boolean
    ): Call<ResponseVO> {
        val request = if (serializeNulls) profileItemsJsonAdapter.serializeNulls().toJson(
            ProfileItemsRequestContainer().addProfileItems(profileItems)
        ) else toJson(RequestContainer().addProfileItem(profileItems[0]))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return profileService.safeAddUpdate(requestBody)
    }

    fun deleteProfileItem(profileItem: ProfileItem): Call<ResponseVO> {
        val request = toJson(RequestContainer().addProfileItem(profileItem))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return profileService.delete(requestBody)
    }

    fun getPaymentIntent(
        accountId: Int,
        accountEmail: String?,
        accountName: String?,
        isAnonymous: Boolean?,
        donationAmount: Int
    ): Call<ResponseVO> {
        return storageService.getPaymentIntent(
            BuildConfig.PAYMENT_INTENT_URL,
            accountId,
            accountEmail,
            accountName,
            isAnonymous,
            donationAmount
        )
    }

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }

    private fun toJson(container: SimpleRequestContainer): String {
        return simpleJsonAdapter.toJson(container)
    }
}