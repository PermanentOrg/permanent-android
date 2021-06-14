package org.permanent.permanent.network

import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.permanent.permanent.BuildEnvOption
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

class NetworkClient {
    private val baseUrl: String = if (Constants.BUILD_ENV == BuildEnvOption.STAGING)
        Constants.URL_STAGING else Constants.URL_PROD
    private val retrofit: Retrofit
    private val authService: IAuthService
    private val accountService: IAccountService
    private val fileService: IFileService
    private val shareService: IShareService
    private val memberService: IMemberService
    private val notificationService: INotificationService
    private val invitationService: IInvitationService
    private val locationService: ILocationService
    private val tagService: ITagService
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val simpleJsonAdapter: JsonAdapter<SimpleRequestContainer>
    private val jsonMediaType: MediaType = Constants.MEDIA_TYPE_JSON.toMediaType()

    companion object {
        val instance = NetworkClient()
    }

    init {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(),
            SharedPrefsCookiePersistor(PermanentApplication.instance.applicationContext))

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.NONE

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(interceptor)
            .addInterceptor(RequiresMFAInterceptor())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()

        authService = retrofit.create(IAuthService::class.java)
        accountService = retrofit.create(IAccountService::class.java)
        fileService = retrofit.create(IFileService::class.java)
        shareService = retrofit.create(IShareService::class.java)
        memberService = retrofit.create(IMemberService::class.java)
        notificationService = retrofit.create(INotificationService::class.java)
        invitationService = retrofit.create(IInvitationService::class.java)
        locationService = retrofit.create(ILocationService::class.java)
        tagService = retrofit.create(ITagService::class.java)
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
        simpleJsonAdapter = Moshi.Builder().build().adapter(SimpleRequestContainer::class.java)
    }

    fun verifyLoggedIn(): Call<ResponseVO> {
        val request = toJson(RequestContainer(""))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return authService.verifyLoggedIn(requestBody)
    }

    fun login(email: String, password: String): Call<ResponseVO> {
        val request: String = toJson(
            RequestContainer("").addAccount(email).addAccountPassword(password)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.login(requestBody)
    }

    fun logout(csrf: String?): Call<ResponseVO> {
        val request: String = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.logout(requestBody)
    }

    fun forgotPassword(email: String): Call<ResponseVO> {
        val request: String = toJson(RequestContainer("").addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.forgotPassword(requestBody)
    }

    fun sendSMSVerificationCode(csrf: String?, accountId: Int, email: String): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf).addAccount(accountId, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.sendSMSVerificationCode(requestBody)
    }

    fun verifyCode(code: String, authType: String, csrf: String?, email: String
    ): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf).addAuth(code, authType).addAccount(email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return authService.verifyCode(requestBody)
    }

    fun signUp(fullName: String, email: String, password: String): Call<ResponseVO> {
        val request = toJson(
            RequestContainer("")
                .addAccountPassword(password, password)
                .addAccount(fullName, email)
        )
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.signUp(requestBody)
    }

    fun getAccount(csrf: String?, accountId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addAccount(accountId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.getAccount(requestBody)
    }

    fun updateAccount(csrf: String?, account: Account): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addAccount(account))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.updateAccount(requestBody)
    }

    fun changePassword(
        csrf: String?, accountId: Int, currentPassword: String, newPassword: String,
        retypedPassword: String
    ): Call<ResponseVO> {
        val request = toJson(
            RequestContainer(csrf)
                .addAccount(accountId)
                .addAccountPassword(currentPassword, newPassword, retypedPassword))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.changePassword(requestBody)
    }

    fun getRoot(csrf: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRoot(requestBody)
    }

    fun navigateMin(csrf: String?, archiveNr: String, folderLinkId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addFolder(archiveNr, folderLinkId, null))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.navigateMin(requestBody)
    }

    fun getLeanItems(
        csrf: String?,
        archiveNumber: String,
        folderLinkId: Int,
        sort: String?,
        childItems: List<Int>
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf)
            .addFolder(archiveNumber, folderLinkId, sort, childItems))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getLeanItems(requestBody)
    }

    fun createFolder(csrf: String?, name: String, folderId: Int, folderLinkId: Int
    ): Call<ResponseVO> {
        val json = toJson(RequestContainer(csrf).addFolder(name, folderId, folderLinkId))
        val requestBody: RequestBody = json.toRequestBody(jsonMediaType)

        return fileService.createFolder(requestBody)
    }

    fun getPresignedUrlForUpload(
        csrf: String?,
        file: File,
        displayName: String,
        folderId: Int,
        folderLinkId: Int,
        mediaType: MediaType
    ): Call<GetPresignedUrlResponse> {
        val request = toJson(RequestContainer(csrf)
            .addRecord(displayName, file, folderId, folderLinkId)
            .addSimple(mediaType))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getPresignedUrl(requestBody)
    }

    fun uploadFile(
        file: File, mediaType: MediaType, uploadDestination: UploadDestination, listener: CountingRequestListener
    ): Call<ResponseBody> {
        val url = uploadDestination.presignedPost?.url
        val fields: Map<String, RequestBody>? = uploadDestination.presignedPost?.getFieldsMapForCall()
        val contentType = (mediaType.toString()).toRequestBody(MultipartBody.FORM)
        val fileRequestBody = CountingRequestBody(file.asRequestBody(mediaType), listener)
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file", file.name, fileRequestBody)

        return fileService.upload(url!!, fields!!, contentType, body)
    }

    fun registerRecord(
        csrf: String?,
        file: File,
        displayName: String,
        folderId: Int,
        folderLinkId: Int,
        s3Url: String
    ): Call<ResponseVO> {
        val request = toJson(SimpleRequestContainer(csrf)
            .addRecord(displayName, file, folderId, folderLinkId)
            .addSimple(s3Url))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.registerRecord(requestBody)
    }

    fun getRecord(
        csrf: String?,
        folderLinkId: Int?,
        recordId: Int?
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(folderLinkId, recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRecord(requestBody)
    }

    fun downloadFile(url: String): Call<ResponseBody> {
        return fileService.download(url)
    }

    fun deleteRecord(csrf: String?, record: Record): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(record))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (record.type == RecordType.FOLDER) {
            fileService.deleteFolder(requestBody)
        } else {
            fileService.deleteRecord(requestBody)
        }
    }

    fun relocateRecord(
        csrf: String?, recordToRelocate: Record, destFolderLinkId: Int, relocationType: RelocationType)
            : Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(recordToRelocate).addFolderDest(destFolderLinkId))
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

    fun updateRecord(csrf: String?, fileData: FileData): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(fileData))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return fileService.updateRecord(requestBody)
    }

    fun updateRecord(csrf: String?, locnVO: LocnVO, fileData: FileData): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(locnVO, fileData))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return fileService.updateRecord(requestBody)
    }

    fun requestShareLink(csrf: String?, record: Record, shareRequestType: ShareRequestType
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(record))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.GET) {
            shareService.getShareLink(requestBody)
        } else {
            shareService.generateShareLink(requestBody)
        }
    }

    fun modifyShareLink(csrf: String?, shareVO: Shareby_urlVO, shareRequestType: ShareRequestType
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShareByUrl(shareVO))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.DELETE) {
            shareService.deleteShareLink(requestBody)
        } else {
            shareService.updateShareLink(requestBody)
        }
    }

    fun approveShare(csrf: String?, share: Share): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShare(share))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.approveShare(requestBody)
    }

    fun denyShare(csrf: String?, share: Share): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShare(share))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.denyShare(requestBody)
    }

    fun checkShareLink(csrf: String?, urlToken: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShareByUrl(urlToken))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.checkShareLink(requestBody)
    }

    fun requestShareAccess(csrf: String?, urlToken: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShareByUrl(urlToken))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.requestShareAccess(requestBody)
    }

    fun getShares(csrf: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return shareService.getShares(requestBody)
    }

    fun getMembers(csrf: String?, archiveNr: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addArchive(archiveNr))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return memberService.getMembers(requestBody)
    }

    fun addMember(csrf: String?, archiveNr: String?, email: String, accessRole: AccessRole): Call<ResponseVO> {
        val request = toJson(
            RequestContainer(csrf)
                .addArchive(archiveNr)
                .addAccount(email, accessRole))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return memberService.addMember(requestBody)
    }

    fun updateMember(csrf: String?, archiveNr: String?, id: Int, email: String, accessRole: AccessRole): Call<ResponseVO> {
        val request = toJson(
            RequestContainer(csrf)
                .addArchive(archiveNr)
                .addAccount(id, email, accessRole))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return memberService.updateMember(requestBody)
    }

    fun deleteMember(csrf: String?, archiveNr: String?, id: Int, email: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addArchive(archiveNr).addAccount(id, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return memberService.deleteMember(requestBody)
    }

    fun getNotifications(): Call<ResponseVO> {
        val request = toJson(RequestContainer(""))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return notificationService.getNotifications(requestBody)
    }

    fun registerDevice(csrf: String?, token: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addSimple(token))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return notificationService.registerDevice(requestBody)
    }

    fun getInvitations(): Call<ResponseVO> {
        val request = toJson(RequestContainer(""))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return invitationService.getInvitations(requestBody)
    }

    fun sendInvitation(csrf: String?, fullName: String, email: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addInvite(fullName, email))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return invitationService.sendInvitation(requestBody)
    }

    fun updateInvitation(csrf: String?, inviteId: Int, updateType: UpdateType): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addInvite(inviteId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (updateType == UpdateType.REVOKE) {
            invitationService.revokeInvitation(requestBody)
        } else {
            invitationService.resendInvitation(requestBody)
        }
    }

    fun getLocation(csrf: String?, latLng: LatLng): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addLocation(latLng))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return locationService.getLocation(requestBody)
    }

    fun getTagsByArchive(csrf: String?, archiveId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addArchive(archiveId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.getTagsByArchive(requestBody)
    }

    fun createOrLinkTag(csrf: String?, tags: List<Tag>, recordId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addTagNames(tags).addTagLink(recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.createOrLinkTags(requestBody)
    }

    fun unlinkTags(csrf: String?, tags: List<Tag>, recordId: Int): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addTagIds(tags).addTagLink(recordId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return tagService.unlinkTags(requestBody)
    }

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }
    private fun toJson(container: SimpleRequestContainer): String {
        return simpleJsonAdapter.toJson(container)
    }
}