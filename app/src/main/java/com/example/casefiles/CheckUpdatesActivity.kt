package com.example.casefiles
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CheckUpdatesActivity : AppCompatActivity() {

    private lateinit var updatesTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_updates)

        buttonBack = findViewById(R.id.backButton)

        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        updatesTextView = findViewById(R.id.updatesTextView)

        // Retrieve updates from the database
        retrieveUpdates()

        buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun retrieveUpdates() {
        val updatesRef = database.child("updates")

        // Attach a listener to read the data at our updates reference
        updatesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if there are updates available
                if (dataSnapshot.exists()) {
                    // Clear the previous updates
                    updatesTextView.text = ""

                    for (updateSnapshot in dataSnapshot.children) {
                        val update = updateSnapshot.getValue(Update::class.java)
                        if (update != null) {
                            // Display the update information
                            displayUpdateInformation(update)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                showErrorToast("Error loading updates: ${databaseError.message}")
            }
        })
    }


    private fun displayUpdateInformation(update: Update) {
        val updateText = "Station: ${update.station}\nDescription: ${update.description}\n\n"
        updatesTextView.append(updateText)
    }

    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
