package edu.uw.ischool.ryancho7.awty

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var controlButton: Button
    private lateinit var audioPhoneNumberEditText: EditText
    private lateinit var videoPhoneNumberEditText: EditText
    private lateinit var sendAudioButton: Button
    private lateinit var sendVideoButton: Button

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        intervalEditText = findViewById(R.id.intervalEditText)
        controlButton = findViewById(R.id.controlButton)
        audioPhoneNumberEditText = findViewById(R.id.audioPhoneNumberEditText)
        videoPhoneNumberEditText = findViewById(R.id.videoPhoneNumberEditText)
        sendAudioButton = findViewById(R.id.sendAudioButton)
        sendVideoButton = findViewById(R.id.sendVideoButton)

        controlButton.setOnClickListener {
            if (isRunning) stopScheduler() else startScheduler()
        }

        sendAudioButton.setOnClickListener {
            sendMmsFile(audioPhoneNumberEditText.text.toString().trim(), R.raw.sound, "audio/mp3")
        }

        sendVideoButton.setOnClickListener {
            sendMmsFile(videoPhoneNumberEditText.text.toString().trim(), R.raw.banana, "video/mp4")
        }
    }

    private fun startScheduler() {
        val message = messageEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val interval = intervalEditText.text.toString().toIntOrNull()

        if (message.isEmpty() || phoneNumber.isEmpty() || interval == null || interval <= 0) {
            Toast.makeText(this, "Please enter valid inputs", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            return
        }

        executor.scheduleAtFixedRate({
            runOnUiThread { sendSms(phoneNumber, message) }
        }, 0, interval.toLong(), TimeUnit.MINUTES)

        isRunning = true
        controlButton.text = getString(R.string.button_stop_text)
    }

    private fun stopScheduler() {
        executor.shutdownNow()
        isRunning = false
        controlButton.text = getString(R.string.button_start_text)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
            Log.d("SMSSend", "SMS sent to $phoneNumber: $message")
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendMmsFile(phoneNumber: String, fileResId: Int, mimeType: String) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return
        }
        val file = copyResourceToCache(fileResId, if (mimeType.startsWith("audio")) "audio.mp3" else "video.mp4")
        val uri = getFileUri(file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mmsto:$phoneNumber")
            putExtra("address", phoneNumber)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send file: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun copyResourceToCache(resourceId: Int, fileName: String): File {
        val file = File(cacheDir, fileName)
        resources.openRawResource(resourceId).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScheduler()
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val SMS_PERMISSION_CODE = 1
    }
}