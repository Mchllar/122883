package com.example.casefiles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CreateUpdatesActivity : AppCompatActivity() {
    private lateinit var stationCardView: CardView
    private lateinit var informationCardView: CardView
    private lateinit var shareButton: Button
    private lateinit var buttonBack: Button
    private lateinit var stationEditText: EditText
    private lateinit var descriptionEditText: EditText

    // Firebase Realtime Database reference
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_updates)

        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        informationCardView = findViewById(R.id.informationCardView)
        stationCardView = findViewById(R.id.stationCardView)
        buttonBack = findViewById(R.id.backButton) // Initialize buttonBack
        shareButton = findViewById(R.id.shareButton) // Initialize shareButton
        stationEditText = findViewById(R.id.stationEditText)
        descriptionEditText = findViewById(R.id.informationEditText)

        // Set click listener for the back button
        buttonBack.setOnClickListener {
            val intent = Intent(this, PoliceActivity::class.java)
            startActivity(intent)
            finish()
        }

        shareButton.setOnClickListener {
            uploadUpdate()
        }
    }

    private fun showAuthenticationErrorToast() {
        Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
    }

    private fun uploadUpdate() {
        // Retrieve the report data from the form
        val station = stationEditText.text.toString()
        val description = descriptionEditText.text.toString()

        // Check if any of the fields are empty
        if (station.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current date and time
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        // Create a unique key for the report
        val updateId = database.child("updates").push().key

        // Check if a report key is generated
        if (updateId != null) {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Create a report object with the retrieved data and user's UID
                val update = Update(station, description, date, time, currentUser.uid)

                // Save the report data to the database
                database.child("updates").child(updateId).setValue(update)

                // Provide feedback to the user
                Toast.makeText(this, "Update submitted", Toast.LENGTH_SHORT).show()

                // Clear the form fields after submission
                clearFormFields()
            } else {
                // Handle the case where the user is not authenticated
                showAuthenticationErrorToast()
            }
        }
    }

    // Clear form fields
    private fun clearFormFields() {
        stationEditText.text.clear()
        descriptionEditText.text.clear()
        // Clear any other form fields if needed
    }
}
