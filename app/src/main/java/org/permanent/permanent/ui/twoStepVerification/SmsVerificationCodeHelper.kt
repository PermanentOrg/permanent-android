package org.permanent.permanent.ui.twoStepVerification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import java.util.regex.Pattern

const val CODE_GROUP_NAME = "code"

class SmsVerificationCodeHelper(val context: Context) {

    private val verificationCode = MutableLiveData<String>()
    private val smsBroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action.equals(Constants.SMS_RECEIVED_ACTION)) {
                val bundle = intent.extras

                if (bundle != null) {
                    val pdusObj = bundle["pdus"] as Array<*>?
                    val currentMessage = SmsMessage.createFromPdu(pdusObj?.get(0) as ByteArray)
                    val textMessage = currentMessage.displayMessageBody

                    //sms example
                    //Verification code:\n1416\n\nPlease enter this one-time code on the Permanent
                    //Foundation website to confirm your phone number

                    val regex = "Verification code:\n(?<code>\\d{4}).*"
                    val pattern = Pattern.compile(regex)
                    val matcher = pattern.matcher(textMessage)

                    if (matcher.find()) {
                        matcher.group(CODE_GROUP_NAME)?.let { verificationCode.value = it }
                    }
                }
            }
        }
    }

    fun getCode(): MutableLiveData<String> {
        return verificationCode
    }

    fun registerReceiver() {
        val broadcastIntent = IntentFilter()
        broadcastIntent.addAction(Constants.SMS_RECEIVED_ACTION)
        broadcastIntent.priority = 1000
        context.registerReceiver(smsBroadcastReceiver, broadcastIntent)
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(smsBroadcastReceiver)
    }
}