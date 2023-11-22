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
import android.app.AlertDialog


class ViewHistoryIncidentsActivity : AppCompatActivity() {

    private lateinit var casesListView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_history_incidents)

        buttonBack = findViewById(R.id.backButton)
        casesListView = findViewById(R.id.casesListView)

        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Retrieve reported cases from the database
        retrieveReportedCases()

        buttonBack.setOnClickListener {
            val intent = Intent(this, PoliceActivity::class.java)
            startActivity(intent)
        }

        // Set a click listener for the items in the ListView
        casesListView.setOnItemClickListener { _, _, position, _ ->
            // Retrieve the selected report ID
            val selectedReportId = casesListView.getItemAtPosition(position) as String

            // Call a function to display full details of the selected report
            displayReportDetails(selectedReportId)
        }
    }

    private fun retrieveReportedCases() {
        val casesRef = database.child("reports")

        casesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reportsList = mutableListOf<String>()

                for (caseSnapshot in dataSnapshot.children) {
                    val report = caseSnapshot.getValue(Report::class.java)
                    if (report != null) {
                        // Add only the report ID to the list
                        reportsList.add(caseSnapshot.key!!)
                    }
                }

                // Create an ArrayAdapter to bind the data to the ListView
                val adapter = ArrayAdapter(
                    this@ViewHistoryIncidentsActivity,
                    android.R.layout.simple_list_item_1,
                    reportsList
                )
                casesListView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                showErrorToast("Error loading reported cases: ${databaseError.message}")
            }
        })
    }

    private fun displayReportDetails(reportId: String) {
        // Retrieve the full details of the selected report using the report ID
        val reportRef = database.child("reports").child(reportId)

        reportRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val report = dataSnapshot.getValue(Report::class.java)

                if (report != null) {
                    // Extract additional information
                    val userId = report.userId
                    val time = report.time
                    val date = report.date

                    // Display the full details of the report in a dialog
                    val fullDetails =
                        "Location: ${report.location}\nStation: ${report.station}\nDescription: ${report.description}\n" +
                                "User Id: $userId\nTime: $time\n" +
                                "Date: $date"

                    val dialogBuilder = AlertDialog.Builder(this@ViewHistoryIncidentsActivity)
                    dialogBuilder.setTitle("Report Details")
                        .setMessage(fullDetails)
                        .setPositiveButton("Review Report") { dialog, _ ->
                            // Navigate to ReviewReportsActivity
                            val reviewIntent = Intent(this@ViewHistoryIncidentsActivity, ReviewReportsActivity::class.java)
                            reviewIntent.putExtra("reportId", reportId)
                            startActivity(reviewIntent)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                showErrorToast("Error loading report details: ${databaseError.message}")
            }
        })
    }




    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
