package edu.uw.ischool.ryancho7.awty

import android.content.Intent
import android.os.Bundle
import android.service.controls.templates.ControlButton
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.uw.ischool.ryancho7.awty.service.NaggingService
import java.nio.channels.InterruptedByTimeoutException

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

        // give button a listener
        controlButton.setOnClickListener {
            if(validInput()) {
                toggleActivity()
            } else {
                Toast.makeText(this, "Please enter valid inputs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validInput(): Boolean {
        val message = messageEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val interval = intervalEditText.text.toString()
        // return true only if everything is valid and not empty
        return message.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                interval.isNotEmpty() &&
                interval.toInt() > 0
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
            startService(intent) // start with the intent that has extras
            controlButton.text = resources.getString(R.string.button_stop_text)
            running = true
        }
    }
}