package com.example.casefiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast

class OptionActivity : AppCompatActivity() {
    private lateinit var buttonProceed: Button
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)

        buttonProceed = findViewById(R.id.proceedButton)
        buttonBack = findViewById(R.id.backButton)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        buttonProceed.setOnClickListener {
            val checkedRadioButtonId = radioGroup.checkedRadioButtonId

            if (checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select a user role (Police or Civilian)", Toast.LENGTH_SHORT).show()
            } else {
                val intent = when (checkedRadioButtonId) {
                    R.id.policeRadioButton -> Intent(this, PoliceRegActivity::class.java)
                    R.id.civilianRadioButton -> Intent(this, RegisterActivity::class.java)
                    else -> null
                }
                intent?.let {
                    startActivity(it)
                }
            }
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
