package edu.uw.ischool.ryancho7.awty.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import edu.uw.ischool.ryancho7.awty.receiver.NagBroadcastReceiver

class NaggingService : Service() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("Message") ?: "Are we there yet?"
        val phoneNumber = intent?.getStringExtra("Phone_Number") ?: "(425) 555-1212"
        // if we can't find interval or if it's null, use default val of 1 -> but this shouldn't be possible
        val interval = intent?.getIntExtra("Interval", 1) ?: 1

        // set up alarm manager by saying that we want an ALARM_SERVICE and then typecasting it using "as"
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, NagBroadcastReceiver::class.java).apply {
            putExtra("MESSAGE", message)
            putExtra("PHONE_NUMBER", phoneNumber)
        }.let { intent ->
            // .let allows us to execute another block of code on the intent we created
            // we want to create a pending intent that will trigger when alarmIntent goes off
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            interval * 60 * 1000L,
            alarmIntent
        )
        // recreate the service if it's killed
        return START_STICKY
    }

    override fun onDestroy() {
        // cancel alarm
        alarmManager.cancel(alarmIntent)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
