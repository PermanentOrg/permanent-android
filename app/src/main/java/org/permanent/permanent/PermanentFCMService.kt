package org.permanent.permanent

import android.app.PendingIntent
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
import org.permanent.permanent.ui.activities.SplashActivity
import org.permanent.permanent.ui.fileView.FileActivity
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import kotlin.random.Random

class PermanentFCMService : FirebaseMessagingService() {
    private val TAG = PermanentFCMService::class.java.simpleName

    override fun onNewToken(token: String) {
        Log.e(TAG, "New token: $token")

//        val notificationsRepository: INotificationRepository =
//            NotificationRepositoryImpl(applicationContext)
//
//        notificationsRepository.registerDevice(token, object : IResponseListener {
//
//            override fun onSuccess(message: String?) {
//            }
//
//            override fun onFailed(error: String?) {
//            }
//        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val notificationType = remoteMessage.data[FCMNotificationKey.NOTIFICATION_TYPE]

            if (notificationType == FCMNotificationType.SHARE_NOTIFICATION.toBackendString()) {
                remoteMessage.data[FCMNotificationKey.SOURCE_ARCHIVE_NAME]?.let { sourceArchiveName ->
                    showNotification(sourceArchiveName,
                        getString(R.string.notification_body_share_notification,
                            sourceArchiveName,
                            remoteMessage.data[FCMNotificationKey.SHARED_ITEM_NAME],
                            remoteMessage.data[FCMNotificationKey.TARGET_ARCHIVE_NAME],
                        ), getFileViewIntent(
                            remoteMessage.data[FCMNotificationKey.RECORD_ID]?.toInt(),
                            remoteMessage.data[FCMNotificationKey.ARCHIVE_NR],
                            remoteMessage.data[FCMNotificationKey.FOLDER_LINK_ID]?.toInt()
                        ))
                }
            }
        }

        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            if (title != null && body != null)
                showNotification(title, body, getDefaultContentIntent())
        }
    }

    override fun onDeletedMessages() {
        // TODO:
    }

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

    private fun getFileViewIntent(recordId: Int?, archiveNr: String?, folderLinkId: Int?
    ): PendingIntent? {
        return if (recordId != null && archiveNr != null && folderLinkId != null) {
            val record = Record(recordId, archiveNr, folderLinkId)
            val intent = Intent(applicationContext, FileActivity::class.java)
            intent.putExtra(PARCELABLE_RECORD_KEY, record)
            TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(intent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        } else {
            getDefaultContentIntent()
        }
    }

    private fun getDefaultContentIntent(): PendingIntent? {
        return TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(Intent(applicationContext, SplashActivity::class.java))
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}