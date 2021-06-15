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

class PermanentFCMService : FirebaseMessagingService() {
    private val TAG = PermanentFCMService::class.java.simpleName

    override fun onNewToken(token: String) {
        val prefsHelper = PreferencesHelper(
            applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

        if(prefsHelper.isUserLoggedIn()) {
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
        Log.d(TAG, "Message notification: ${remoteMessage.notification?.body}")
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        if (remoteMessage.data.isNotEmpty()) {
            when (remoteMessage.data[FCMNotificationKey.NOTIFICATION_TYPE]) {
                FCMNotificationType.SHARE.toBackendString() -> {
                    val recordId = if (remoteMessage.data[FCMNotificationKey.RECORD_ID] != null)
                        remoteMessage.data[FCMNotificationKey.RECORD_ID]
                    else remoteMessage.data[FCMNotificationKey.FOLDER_ID]

                    val recordName = if (remoteMessage.data[FCMNotificationKey.RECORD_NAME] != null)
                        remoteMessage.data[FCMNotificationKey.RECORD_NAME]
                    else remoteMessage.data[FCMNotificationKey.FOLDER_NAME]

                    remoteMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let {
                        showNotification(it,
                            getString(R.string.notification_body_share, it, recordName),
                            getRecordViewIntent(recordId?.toInt()))
                    }
                }
                FCMNotificationType.PA_RESPONSE.toBackendString() -> {
                    remoteMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let {
                        val splits = remoteMessage.data[FCMNotificationKey.ACCESS_ROLE]?.split(".")
                        showNotification(it,
                            getString(R.string.notification_body_pa_response, it,
                                remoteMessage.data[FCMNotificationKey.FROM_ARCHIVE_NAME],
                                splits?.get(splits.lastIndex)),
                            getMembersViewIntent())
                    }
                }
                FCMNotificationType.SHARE_LINK_REQUEST.toBackendString() -> {
                    remoteMessage.data[FCMNotificationKey.SHARE_FOLDER_LINK_ID]?.let {
                        requestRecordBy(it.toInt(), remoteMessage)
                    }
                }
                FCMNotificationType.SHARE_INVITATION_ACCEPTANCE.toBackendString() -> {
                    remoteMessage.data[FCMNotificationKey.RECORD_ID]?.let {
                        requestRecordBy(it, remoteMessage)
                    }
                }
            }
        }
    }

    private fun requestRecordBy(folderLinkId: Int, remoteMessage: RemoteMessage) {
        val fileRepository: IFileRepository = FileRepositoryImpl(application)

        fileRepository.getRecord(folderLinkId, null).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val record = response.body()?.getRecord()
                if (record != null) {
                    remoteMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let {
                        showNotification(
                            it,
                            getString(R.string.notification_body_share_link_request, it,
                                remoteMessage.data[FCMNotificationKey.SHARE_NAME]),
                            getShareLinkViewIntent(record)
                        )
                    }
                } else {
                    requestFolderBy(folderLinkId, fileRepository, remoteMessage)
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                Log.d(TAG, "Failed getRecord for notification: ${t.message}")
            }
        })
    }

    private fun requestFolderBy(
        folderLinkId: Int,
        fileRepository: IFileRepository,
        remoteMessage: RemoteMessage
    ) {
        fileRepository.getFolder(folderLinkId, object : IRecordListener {
            override fun onSuccess(record: Record) {
                remoteMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME]?.let {
                    showNotification(
                        it,
                        getString(R.string.notification_body_share_link_request, it,
                            remoteMessage.data[FCMNotificationKey.SHARE_NAME]),
                        getShareLinkViewIntent(record)
                    )
                }
            }

            override fun onFailed(error: String?) {
                Log.d(TAG, "Failed getFolder for notification: $error")
            }
        })
    }

    private fun requestRecordBy(recordId: String, remoteMessage: RemoteMessage) {
        val fileRepository: IFileRepository = FileRepositoryImpl(application)

        fileRepository.getRecord(null, recordId.toInt())
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    response.body()?.getRecord()?.let { record ->
                        remoteMessage.data[FCMNotificationKey.INVITED_ARCHIVE_NAME]?.let {
                            showNotification(
                                it,
                                getString(R.string.notification_body_share_invitation_acceptance, it,
                                    remoteMessage.data[FCMNotificationKey.RECORD_NAME]),
                                getShareLinkViewIntent(record)
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    Log.d(TAG, "Failed getRecord for notification: ${t.message}")
                }
            })
    }

    override fun onDeletedMessages() {}

    private fun showNotification(title: String, body: String, contentIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(applicationContext,
            getString(R.string.default_notification_channel_id))
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

    private fun getRecordViewIntent(recordId: Int?): PendingIntent? {
        return if (recordId != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.sharesFragment)
            intent.putExtra(CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY, Constants.POSITION_SHARED_WITH_ME_FRAGMENT)
            intent.putExtra(RECORD_ID_TO_NAVIGATE_TO_KEY, recordId)
            getPendingIntent(intent)
        } else {
            getDefaultContentIntent()
        }
    }

    private fun getMembersViewIntent(): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.membersFragment)
        return getPendingIntent(intent)
    }

    private fun getShareLinkViewIntent(record: Record): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(PARCELABLE_RECORD_KEY, record)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.shareLinkFragment)
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
            return getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}