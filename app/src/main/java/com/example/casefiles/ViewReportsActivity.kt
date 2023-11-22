package com.example.casefiles

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewReportsActivity: AppCompatActivity() {

    private lateinit var casesListView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reports)

        buttonBack = findViewById(R.id.backButton)
        casesListView = findViewById(R.id.casesListView)

        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Retrieve reported cases specific to the current user from the database
        retrieveUserReportedCases()

        buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun retrieveUserReportedCases() {
        val currentUser = auth.currentUser
        val casesRef = database.child("reports")

        if (currentUser != null) {
            // Filter reports based on the user's UID
            casesRef.orderByChild("uid").equalTo(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val reportsList = mutableListOf<String>()

                    for (caseSnapshot in dataSnapshot.children) {
                        val report = caseSnapshot.getValue(Report::class.java)
                        if (report != null) {
                            val caseText = "Location: ${report.location}\nStation: ${report.station}\nDescription: ${report.description}\n\n"
                            reportsList.add(caseText)
                        }
                    }

                    // Create an ArrayAdapter to bind the data to the ListView
                    val adapter = ArrayAdapter(this@ViewReportsActivity, android.R.layout.simple_list_item_1, reportsList)
                    casesListView.adapter = adapter

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    showErrorToast("Error loading reported cases: ${databaseError.message}")
                }
            })
        }
    }



    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
