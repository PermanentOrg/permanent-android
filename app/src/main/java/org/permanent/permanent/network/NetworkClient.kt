package org.permanent.permanent.network

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.invitations.UpdateType
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestBody
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class NetworkClient(context: Context) {
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
    private val jsonAdapter: JsonAdapter<RequestContainer>
    private val jsonMediaType: MediaType = Constants.MEDIA_TYPE_JSON.toMediaType()
    private val uploadUrl = if (Constants.BUILD_ENV === BuildEnvOption.STAGING)
        Constants.URL_UPLOAD_STAGING else Constants.URL_UPLOAD_PROD

    init {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(context.applicationContext)
        )

        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(interceptor)
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
        jsonAdapter = Moshi.Builder().build().adapter(RequestContainer::class.java)
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

    fun sendSMSVerificationCode(csrf: String?, accountId: Int, email: String
    ): Call<ResponseVO>  {
        val request = toJson(RequestContainer(csrf)
            .addAccount(accountId, email, null))
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

    fun update(
        csrf: String?, accountId: Int, email: String, phoneNumber: String
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addAccount(accountId, email, phoneNumber))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return accountService.updatePhone(requestBody)
    }

    fun getRoot(csrf: String?): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getRoot(requestBody)
    }

    fun navigateMin(csrf: String?, archiveNumber: String): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addFolder(archiveNumber, null))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.navigateMin(requestBody)
    }

    fun getLeanItems(csrf: String?, archiveNumber: String, sort: String?, childItems: List<Int>
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addFolder(archiveNumber, sort, childItems))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.getLeanItems(requestBody)
    }

    fun createFolder(csrf: String?, name: String, folderId: Int, folderLinkId: Int
    ): Call<ResponseVO> {
        val json = toJson(RequestContainer(csrf).addFolder(name, folderId, folderLinkId))
        val requestBody: RequestBody = json.toRequestBody(jsonMediaType)

        return fileService.createFolder(requestBody)
    }

    fun createUploadMetaData(
        csrf: String?, fileName: String, displayName: String?, folderId: Int,
        folderLinkId: Int
    ): Call<ResponseVO> {
        val request =
            toJson(RequestContainer(csrf).addRecord(displayName, fileName, folderId, folderLinkId))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)

        return fileService.postMeta(requestBody)
    }

    fun uploadFile(
        file: File, mediaType: MediaType, recordId: Int, listener: CountingRequestListener
    ): Call<ResponseBody> {
        val recordIdRequestBody = recordId.toString().toRequestBody(MultipartBody.FORM)
        val fileRequestBody = CountingRequestBody(file.asRequestBody(mediaType), listener)
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            Constants.FORM_DATA_NAME_THE_FILE, file.name, fileRequestBody)

        return fileService.upload(uploadUrl, recordIdRequestBody, body)
    }

    fun getFile(
        csrf: String?,
        folderLinkId: Int,
        archiveNr: String,
        archiveId: Int,
        recordId: Int
    ): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf)
            .addRecord(folderLinkId, archiveNr, archiveId, recordId))
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

    fun requestShareLink(csrf: String?, record: Record, shareRequestType: ShareRequestType): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addRecord(record))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.GET) {
            shareService.getShareLink(requestBody)
        } else {
            shareService.generateShareLink(requestBody)
        }
    }

    fun modifyShareLink(csrf: String?, shareVO: Shareby_urlVO, shareRequestType: ShareRequestType): Call<ResponseVO> {
        val request = toJson(RequestContainer(csrf).addShareByUrl(shareVO))
        val requestBody: RequestBody = request.toRequestBody(jsonMediaType)
        return if (shareRequestType == ShareRequestType.DELETE) {
            shareService.deleteShareLink(requestBody)
        } else {
            shareService.updateShareLink(requestBody)
        }
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

    private fun toJson(container: RequestContainer): String {
        return jsonAdapter.toJson(container)
    }
}