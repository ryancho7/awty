package edu.uw.ischool.ryancho7.awty

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import edu.uw.ischool.ryancho7.awty.service.NaggingService

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var controlButton: Button
    private var running = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        intervalEditText = findViewById(R.id.intervalEditText)
        controlButton = findViewById(R.id.controlButton)

        controlButton.setOnClickListener {
            if (validInput()) {
                checkForSmsPermission()
            } else {
                Toast.makeText(this, "Please enter valid inputs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validInput(): Boolean {
        val message = messageEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val interval = intervalEditText.text.toString()
        return message.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                interval.isNotEmpty() &&
                interval.toInt() > 0
    }

    private fun checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity", "Permission not granted!")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.SEND_SMS), 1
            )
        } else {
            toggleActivity()
        }
    }

    private fun toggleActivity() {
        val intent = Intent(this, NaggingService::class.java).apply {
            putExtra("Message", messageEditText.text.toString())
            putExtra("Phone_Number", phoneNumberEditText.text.toString())
            putExtra("Interval", intervalEditText.text.toString().toInt())
        }

        if (running) {
            stopService(intent)
            controlButton.text = resources.getString(R.string.button_start_text)
            running = false
        } else {
            startService(intent)
            controlButton.text = resources.getString(R.string.button_stop_text)
            running = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (permissions[0] == Manifest.permission.SEND_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toggleActivity()
                } else {
                    Log.d("MainActivity", "Failed to obtain SMS permission")
                    Toast.makeText(
                        this,
                        "SMS permission is required to send messages",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}