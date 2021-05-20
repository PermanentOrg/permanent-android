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
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.activities.SplashActivity
import org.permanent.permanent.ui.shares.CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY
import org.permanent.permanent.ui.shares.RECORD_ID_TO_NAVIGATE_TO_KEY
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
            val notificationType = remoteMessage.data[FCMNotificationKey.NOTIFICATION_TYPE]

            if (notificationType == FCMNotificationType.SHARE.toBackendString()) {
                val recordId = if (remoteMessage.data[FCMNotificationKey.RECORD_ID] != null)
                    remoteMessage.data[FCMNotificationKey.RECORD_ID]
                else remoteMessage.data[FCMNotificationKey.FOLDER_ID]

                val recordName = if (remoteMessage.data[FCMNotificationKey.RECORD_NAME] != null)
                    remoteMessage.data[FCMNotificationKey.RECORD_NAME]
                else remoteMessage.data[FCMNotificationKey.FOLDER_NAME]

                showNotification(getString(R.string.notification_body_share_notification,
                    remoteMessage.data[FCMNotificationKey.FROM_ACCOUNT_NAME], recordName,
                ), getRecordViewIntent(recordId?.toInt()))
            }
        }
    }

    override fun onDeletedMessages() {}

    private fun showNotification(body: String, contentIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(applicationContext,
            getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.img_notification_logo)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
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

    private fun getDefaultContentIntent(): PendingIntent? {
        return getPendingIntent(Intent(applicationContext, SplashActivity::class.java))
    }

    private fun getPendingIntent(intent: Intent): PendingIntent? {
        TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack
            return getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}