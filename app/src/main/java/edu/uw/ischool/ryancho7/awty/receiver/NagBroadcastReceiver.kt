package edu.uw.ischool.ryancho7.awty.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.util.Log

class NagBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("MESSAGE") ?: "Are we there yet?"
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: "(425) 555-1212"
        Toast.makeText(context, "$phoneNumber: $message", Toast.LENGTH_SHORT).show()
    }
}
