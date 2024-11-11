package edu.uw.ischool.ryancho7.awty.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

class NagBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("MESSAGE") ?: "Are we there yet?"
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "(425) 555-1212"

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("NagBroadcastReceiver", "SMS sent to $phoneNumber: $message")
        } catch (e: Exception) {
            Log.e("NagBroadcastReceiver", "Failed to send SMS", e)
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}