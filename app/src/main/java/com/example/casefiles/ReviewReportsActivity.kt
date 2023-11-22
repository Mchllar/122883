package com.example.casefiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewReportsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var commentsEditText: EditText
    private lateinit var updateStatusButton: Button
    private lateinit var crimeDetailsTextView: TextView
    private lateinit var reporterDetailsTextView: TextView
    private lateinit var reporterPhoneDetailsTextView: TextView

    private lateinit var reportImageView: ImageView
    private lateinit var reportVideoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_reports)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        reportImageView = findViewById(R.id.reportImageView)
        reportVideoView = findViewById(R.id.reportVideoView)

        // Retrieve report ID from the intent
        val reportId = intent.getStringExtra("reportId")

        // Check if reportId is null
        if (reportId != null) {
            // Initialize UI elements
            commentsEditText = findViewById(R.id.commentsEditText)
            updateStatusButton = findViewById(R.id.updateStatusButton)
            crimeDetailsTextView = findViewById(R.id.crimeDetailsTextView)
            reporterDetailsTextView = findViewById(R.id.reporterDetailsTextView)
            reporterPhoneDetailsTextView = findViewById(R.id.reporterPhoneDetailsTextView)
            val statusSpinner: Spinner = findViewById(R.id.statusSpinner)

            // Set up the status options in the spinner
            val statusAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                this,
                R.array.status_options,
                android.R.layout.simple_spinner_item
            )
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = statusAdapter

            // Retrieve and display crime details and reporter details
            retrieveCrimeAndReporterDetails(reportId)

            // Set a click listener for the Update Status button
            updateStatusButton.setOnClickListener {
                val comments = commentsEditText.text.toString()
                val selectedStatus = statusSpinner.selectedItem.toString()
                updateReportDetails(reportId, comments, selectedStatus)
            }

            reporterPhoneDetailsTextView.setOnClickListener {
                makePhoneCall()
            }
        } else {
            // Handle the case where reportId is null
            showErrorToast("Report ID not found")
            finish()
        }
    }

    private fun retrieveCrimeAndReporterDetails(reportId: String?) {
        if (reportId != null) {
            val reportsRef = database.child("reports").child(reportId)

            reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val report = dataSnapshot.getValue(Report::class.java)

                    if (report != null) {
                        // Display crime details
                        val crimeDetails =
                            "Location: ${report.location}\nStation: ${report.station}\nDescription: ${report.description}\nImage: ${report.imageUrl}\nVideo: ${report.videoUrl}"
                        crimeDetailsTextView.text = crimeDetails

                        // Display reporter details
                        getUserDetails(reporterDetailsTextView, report.userId)
                        getPhoneDetails(reporterPhoneDetailsTextView, report.userId)

                        // Display image if available
                        if (report.imageUrl.isNotEmpty()) {
                            reportImageView.visibility = View.VISIBLE
                            Glide.with(this@ReviewReportsActivity)
                                .load(report.imageUrl)
                                .into(reportImageView)
                        }

                        // Display video if available
// Display video if available
                        if (report.videoUrl.isNotEmpty()) {
                            reportVideoView.visibility = View.VISIBLE
                            val videoUri = Uri.parse(report.videoUrl)

                            // Set the video URI to the VideoView
                            reportVideoView.setVideoURI(videoUri)

                            // Start the video playback
                            reportVideoView.start()

                            // Set an OnPreparedListener to handle video preparation
                            reportVideoView.setOnPreparedListener { mediaPlayer ->
                                // Adjust the video size based on the aspect ratio
                                val videoWidth = mediaPlayer.videoWidth
                                val videoHeight = mediaPlayer.videoHeight
                                val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
                                val screenWidth = resources.displayMetrics.widthPixels
                                val screenHeight = resources.displayMetrics.heightPixels
                                val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
                                val videoLayoutParams = reportVideoView.layoutParams

                                if (videoProportion > screenProportion) {
                                    videoLayoutParams.width = screenWidth
                                    videoLayoutParams.height = (screenWidth / videoProportion).toInt()
                                } else {
                                    videoLayoutParams.width = (videoProportion * screenHeight).toInt()
                                    videoLayoutParams.height = screenHeight
                                }

                                // Commit the layout parameters
                                reportVideoView.layoutParams = videoLayoutParams
                            }
                        }


                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    showErrorToast("Error loading details: ${databaseError.message}")
                }
            })
        }
    }

    private fun getUserDetails(
        reporterDetailsTextView: TextView,
        userId: String
    ) {
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(UserDetails::class.java)

                    if (user != null) {

                        val nationalIdText = getString(R.string.user_national_id_label, user.nationalId)
                        val nameText = getString(R.string.user_name_label, user.name)

                        val userDetailsText = getString(R.string.user_details_format, nationalIdText, nameText)
                        reporterDetailsTextView.text = userDetailsText

                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ReviewReportsActivity, "Error loading user details", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(this@ReviewReportsActivity, "Account was Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                val errorMessage = getString(R.string.database_error, databaseError.message)
                Toast.makeText(this@ReviewReportsActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getPhoneDetails(
        reporterPhoneDetailsTextView: TextView,
        userId: String
    ) {
        val userRef = database.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(UserDetails::class.java)

                    if (user != null) {
                        // Ensure that UserDetails class has a phoneNumber property
                        val phoneNumberText = getString(R.string.user_phone_format, user.phoneNumber)

                        reporterPhoneDetailsTextView.text = phoneNumberText
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                val errorMessage = getString(R.string.database_error, databaseError.message)
                Toast.makeText(this@ReviewReportsActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun makePhoneCall() {
        val phoneNumber = reporterPhoneDetailsTextView.text.toString()

        // Remove non-numeric characters from the phone number
        val cleanedPhoneNumber = phoneNumber.replace("[^0-9]".toRegex(), "")

        if (cleanedPhoneNumber.isNotEmpty()) {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$cleanedPhoneNumber")
            startActivity(dialIntent)
        } else {
            Toast.makeText(this@ReviewReportsActivity, "Phone number not available", Toast.LENGTH_SHORT).show()
        }
    }




    private fun updateReportDetails(reportId: String?, comments: String, status: String) {
        // Update the report status and comments in the database
        if (reportId != null) {
            val reportsRef = database.child("reports").child(reportId)
            reportsRef.child("status").setValue(status)
            reportsRef.child("comments").setValue(comments)
        }

        // Optionally, you can navigate back to the previous activity or perform other actions
        finish()
    }


    private fun showErrorToast(errorMessage: String) {
        // You can customize this function to display errors as needed
        Toast.makeText(this@ReviewReportsActivity, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
