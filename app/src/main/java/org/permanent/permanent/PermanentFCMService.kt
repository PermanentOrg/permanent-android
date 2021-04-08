package org.permanent.permanent

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class PermanentFCMService : FirebaseMessagingService() {
    private val TAG = PermanentFCMService::class.java.simpleName

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO:
//        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            // TODO: handle data
        }

        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            if (title != null && body != null) showNotification(title, body) }
    }

    private fun showNotification(title: String, body: String) {
        val builder = NotificationCompat.Builder(applicationContext,
            getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.img_notification_logo)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    override fun onDeletedMessages() {
        // TODO:
    }
}