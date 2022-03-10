package org.permanent.permanent

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.permanent.permanent.models.FCMNotificationKey
import org.permanent.permanent.models.FCMNotificationType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.activities.SplashActivity
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.shares.CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY
import org.permanent.permanent.ui.shares.RECORD_ID_TO_NAVIGATE_TO_KEY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

const val START_DESTINATION_FRAGMENT_ID_KEY = "start_destination_fragment_id_key"
const val RECIPIENT_ARCHIVE_NR_KEY = "recipient_archive_nr_key"
const val RECIPIENT_ARCHIVE_NAME_KEY = "recipient_archive_name_key"

class PermanentFCMService : FirebaseMessagingService() {
    private val TAG = PermanentFCMService::class.java.simpleName
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var fcmMessage: RemoteMessage

    override fun onNewToken(token: String) {
        prefsHelper = PreferencesHelper(
            applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        if (prefsHelper.isUserLoggedIn()) {
            val notificationsRepository: INotificationRepository =
                NotificationRepositoryImpl(applicationContext)

            notificationsRepository.registerDevice(token, object : IResponseListener {

                override fun onSuccess(message: String?) {
                }

                override fun onFailed(error: String?) {
                    Log.d(TAG, "Failed registering the new device token: $error")
                }
            })
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        fcmMessage = remoteMessage
        Log.d(TAG, "Message notification: ${fcmMessage.notification?.body}")
        Log.d(TAG, "Message data payload: ${fcmMessage.data}")
        prefsHelper = PreferencesHelper(
            applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        if (fcmMessage.data.isNotEmpty()) {
            when (fcmMessage.data[FCMNotificationKey.NOTIFICATION_TYPE]) {

                FCMNotificationType.SHARE.toBackendString() -> {
                    fcmMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let { fromAccountName ->
                        fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NUMBER]?.let { toArchiveNr ->
                            fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NAME]?.let { toArchiveName ->
                                var recordId = fcmMessage.data[FCMNotificationKey.RECORD_ID]
                                if (recordId == null) recordId =
                                    fcmMessage.data[FCMNotificationKey.FOLDER_ID]

                                var recordName = fcmMessage.data[FCMNotificationKey.RECORD_NAME]
                                if (recordName == null) recordName =
                                    fcmMessage.data[FCMNotificationKey.FOLDER_NAME]

                                val notificationBody =
                                    if (fcmMessage.data[FCMNotificationKey.RECORD_NAME] != null)
                                        getString(
                                            R.string.notification_body_share_file,
                                            fromAccountName,
                                            recordName
                                        )
                                    else getString(
                                        R.string.notification_body_share_folder,
                                        fromAccountName,
                                        recordName
                                    )

                                showNotification(
                                    fromAccountName,
                                    notificationBody,
                                    getRecordViewIntent(
                                        recordId?.toInt(),
                                        toArchiveNr,
                                        toArchiveName
                                    )
                                )
                            }
                        }
                    }
                }

                FCMNotificationType.PA_RESPONSE.toBackendString() -> {
                    fcmMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let { fromAccountName ->
                        fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NUMBER]?.let { toArchiveNr ->
                            fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NAME]?.let { toArchiveName ->
                                val splits =
                                    fcmMessage.data[FCMNotificationKey.ACCESS_ROLE]?.split(".")
                                val accessRole = splits?.get(splits.lastIndex)
                                showNotification(
                                    fromAccountName,
                                    getString(
                                        R.string.notification_body_pa_response,
                                        fromAccountName,
                                        fcmMessage.data[FCMNotificationKey.FROM_ARCHIVE_NAME],
                                        accessRole
                                    ),
                                    getMembersViewIntent(toArchiveNr, toArchiveName)
                                )
                            }
                        }
                    }
                }

                FCMNotificationType.SHARE_LINK_REQUEST.toBackendString() -> {
                    fcmMessage.data[FCMNotificationKey.SHARE_FOLDER_LINK_ID]?.let { folderLinkId ->
                        requestRecordBy(
                            folderLinkId.toInt(),
                            FCMNotificationType.SHARE_LINK_REQUEST
                        )
                    }
                }

                FCMNotificationType.SHARE_INVITATION_ACCEPTANCE.toBackendString() -> {
                    fcmMessage.data[FCMNotificationKey.FOLDER_LINK_ID]?.let { folderLinkId ->
                        requestRecordBy(
                            folderLinkId.toInt(),
                            FCMNotificationType.SHARE_INVITATION_ACCEPTANCE
                        )
                    }
                }
            }
        }
    }

    private fun requestRecordBy(
        folderLinkId: Int,
        notificationType: FCMNotificationType
    ) {
        val fileRepository: IFileRepository = FileRepositoryImpl(application)

        fileRepository.getRecord(folderLinkId, null).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val record = response.body()?.getRecord()
                if (record != null) {
                    prepareNotification(record, notificationType)
                } else {
                    requestFolderBy(folderLinkId, fileRepository, notificationType)
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                Log.d(TAG, "Failed getRecord for notification: ${t.message}")
            }
        })
    }

    private fun prepareNotification(
        record: Record,
        notificationType: FCMNotificationType
    ) {
        fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NUMBER]?.let { toArchiveNr ->
            fcmMessage.data[FCMNotificationKey.TO_ARCHIVE_NAME]?.let { toArchiveName ->
                if (notificationType == FCMNotificationType.SHARE_LINK_REQUEST) {
                    fcmMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let {
                        showNotification(
                            it,
                            getString(
                                R.string.notification_body_share_link_request, it,
                                fcmMessage.data[FCMNotificationKey.SHARE_NAME]
                            ),
                            getShareLinkViewIntent(record, toArchiveNr, toArchiveName)
                        )
                    }
                } else { //FCMNotificationType.SHARE_INVITATION_ACCEPTANCE)
                    fcmMessage.data[FCMNotificationKey.INVITED_EMAIL]?.let {
                        val recordName = if (record.type == RecordType.FILE)
                            fcmMessage.data[FCMNotificationKey.RECORD_NAME]
                        else fcmMessage.data[FCMNotificationKey.FOLDER_NAME]
                        showNotification(
                            it,
                            getString(
                                R.string.notification_body_share_invitation_acceptance,
                                it,
                                recordName
                            ),
                            getShareLinkViewIntent(record, toArchiveNr, toArchiveName)
                        )
                    }
                }
            }
        }
    }

    private fun requestFolderBy(
        folderLinkId: Int,
        fileRepository: IFileRepository,
        notificationType: FCMNotificationType
    ) {
        fileRepository.getFolder(folderLinkId, object : IRecordListener {
            override fun onSuccess(record: Record) {
                prepareNotification(record, notificationType)
            }

            override fun onFailed(error: String?) {
                Log.d(TAG, "Failed getFolder for notification: $error")
            }
        })
    }

    override fun onDeletedMessages() {}

    private fun showNotification(title: String, body: String, contentIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.default_notification_channel_id)
        )
            .setSmallIcon(R.drawable.img_notification_logo)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun getRecordViewIntent(
        recordId: Int?,
        recipientArchiveNr: String,
        recipientArchiveName: String
    ): PendingIntent? {
        return if (recordId != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.sharesFragment)
            intent.putExtra(
                CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY,
                Constants.POSITION_SHARED_WITH_ME_FRAGMENT
            )
            intent.putExtra(RECORD_ID_TO_NAVIGATE_TO_KEY, recordId)
            intent.putExtra(RECIPIENT_ARCHIVE_NR_KEY, recipientArchiveNr)
            intent.putExtra(RECIPIENT_ARCHIVE_NAME_KEY, recipientArchiveName)
            getPendingIntent(intent)
        } else {
            getDefaultContentIntent()
        }
    }

    private fun getMembersViewIntent(
        recipientArchiveNr: String,
        recipientArchiveName: String
    ): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.membersFragment)
        intent.putExtra(RECIPIENT_ARCHIVE_NR_KEY, recipientArchiveNr)
        intent.putExtra(RECIPIENT_ARCHIVE_NAME_KEY, recipientArchiveName)
        return getPendingIntent(intent)
    }

    private fun getShareLinkViewIntent(
        record: Record,
        recipientArchiveNr: String,
        recipientArchiveName: String
    ): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(PARCELABLE_RECORD_KEY, record)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.myFilesFragment)
        intent.putExtra(RECIPIENT_ARCHIVE_NR_KEY, recipientArchiveNr)
        intent.putExtra(RECIPIENT_ARCHIVE_NAME_KEY, recipientArchiveName)
        return getPendingIntent(intent)
    }

    private fun getDefaultContentIntent(): PendingIntent? {
        return getPendingIntent(Intent(applicationContext, SplashActivity::class.java))
    }

    private fun getPendingIntent(intent: Intent): PendingIntent? {
        TaskStackBuilder.create(applicationContext).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack
            return getPendingIntent(0,  PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}