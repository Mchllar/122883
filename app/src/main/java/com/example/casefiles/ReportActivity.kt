package com.example.casefiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    // UI elements
    private lateinit var locationCardView: CardView
    private lateinit var stationCardView: CardView
    private lateinit var descriptionCardView: CardView
    private lateinit var locationEditText: EditText
    private lateinit var stationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var buttonBack: Button
    private lateinit var imageButton: Button
    private lateinit var videoButton: Button
    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView

    // Firebase Realtime Database reference
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Firebase Storage reference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    // Image and video variables
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedVideoUri: Uri



    // Activity result launchers
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Initialize UI elements
        locationCardView = findViewById(R.id.locationCardView)
        stationCardView = findViewById(R.id.stationCardView)
        descriptionCardView = findViewById(R.id.descriptionCardView)
        locationEditText = findViewById(R.id.locationEditText)
        stationEditText = findViewById(R.id.stationEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        submitButton = findViewById(R.id.submitButton)
        buttonBack = findViewById(R.id.backButton)
        imageButton = findViewById(R.id.imageButton)
        videoButton = findViewById(R.id.videoButton)
        imageView = findViewById(R.id.imageView)
        videoView = findViewById(R.id.videoView)

        // Set click listener for the back button
        buttonBack.setOnClickListener {
            val user = auth.currentUser

            if (user != null) {
                val uid = user.uid
                // Check user role in the database
                checkUserRole(uid)
            }
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            submitReport()
        }

        imageButton.setOnClickListener {
            openImageChooser()
        }

        // Set click listener for the video button
        videoButton.setOnClickListener {
            openVideoChooser()
        }

        // Initialize activity result launchers
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                imageView.setImageURI(selectedImageUri)
                imageView.visibility = View.VISIBLE
            }
        }

        videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedVideoUri = uri
                videoView.setVideoURI(selectedVideoUri)
                videoView.visibility = View.VISIBLE
            }
        }
    }

    private fun openImageChooser() {
        imagePickerLauncher.launch("image/*")
    }

    private fun openVideoChooser() {
        videoPickerLauncher.launch("video/*")
    }

    private fun showAuthenticationErrorToast() {
        Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
    }

    // Submit the report
    private fun submitReport() {
        // Retrieve the report data from the form
        val location = locationEditText.text.toString()
        val station = stationEditText.text.toString()
        val description = descriptionEditText.text.toString()

        // Check if any of the fields are empty
        if (location.isEmpty() || station.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current date and time
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        // Create a unique key for the report
        val reportId = database.child("reports").push().key

        // Check if a report key is generated
        if (reportId != null) {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Initialize URLs as empty strings
                var imageUrl = ""
                var videoUrl = ""

                // Check if an image is selected
                if (::selectedImageUri.isInitialized) {
                    uploadImageAndGetUrl(reportId) { url ->
                        imageUrl = url

                        // Check if a video is selected after the image upload completes
                        if (::selectedVideoUri.isInitialized) {
                            uploadVideoAndGetUrl(reportId) { videoUrl ->
                                // Create a report object with the retrieved data, user's UID, and image and video URLs
                                val report = Report(
                                    location,
                                    station,
                                    date,
                                    time,
                                    imageUrl,
                                    videoUrl,
                                    description,
                                    currentUser.uid
                                )

                                // Save the report data to the database
                                database.child("reports").child(reportId).setValue(report)

                                // Provide feedback to the user
                                Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()

                                // Clear the form fields after submission
                                clearFormFields()
                            }
                        }
                    }
                } else if (::selectedVideoUri.isInitialized) {
                    // No image selected, but video is selected
                    uploadVideoAndGetUrl(reportId) { videoUrl ->
                        // Create a report object with the retrieved data, user's UID, and image and video URLs
                        val report = Report(
                            location,
                            station,
                            date,
                            time,
                            imageUrl,
                            videoUrl,
                            description,
                            currentUser.uid
                        )

                        // Save the report data to the database
                        database.child("reports").child(reportId).setValue(report)

                        // Provide feedback to the user
                        Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()

                        // Clear the form fields after submission
                        clearFormFields()
                    }
                } else {
                    // No image or video selected
                    // Create a report object with the retrieved data and user's UID
                    val report = Report(
                        location,
                        station,
                        date,
                        time,
                        imageUrl,
                        videoUrl,
                        description,
                        currentUser.uid
                    )

                    // Save the report data to the database
                    database.child("reports").child(reportId).setValue(report)

                    // Provide feedback to the user
                    Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()

                    // Clear the form fields after submission
                    clearFormFields()
                }
            } else {
                // Handle the case where the user is not authenticated
                showAuthenticationErrorToast()
            }
        }
    }

    private fun uploadImageAndGetUrl(reportId: String, callback: (String) -> Unit) {
        val imageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")
        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener {
                // Image upload success
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Store image URL in the database
                    database.child("reports").child(reportId).child("imageUrl")
                        .setValue(imageUrl.toString())

                    // Invoke the callback with the URL
                    callback(imageUrl.toString())
                }
            }
            .addOnFailureListener {
                // Image upload failure
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()

                Log.d("ReportActivity", "Selected Image URI: $selectedImageUri")

            }
    }

    private fun uploadVideoAndGetUrl(reportId: String, callback: (String) -> Unit) {
        val videoRef = storageReference.child("videos/${UUID.randomUUID()}.mp4")
        videoRef.putFile(selectedVideoUri)
            .addOnSuccessListener {
                // Video upload success
                videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                    // Store video URL in the database
                    database.child("reports").child(reportId).child("videoUrl")
                        .setValue(videoUrl.toString())

                    // Invoke the callback with the URL
                    callback(videoUrl.toString())
                }
            }
            .addOnFailureListener {
                // Video upload failure
                Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
            }
    }

    // Clear form fields
    private fun clearFormFields() {
        locationEditText.text.clear()
        stationEditText.text.clear()
        descriptionEditText.text.clear()
        // Clear any other form fields if needed
    }

    private fun checkUserRole(uid: String) {
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists in "users" table, redirect to MainActivity
                        redirectToMainActivity()

                    } else {
                        // UID does not exist in "users" table, check the "police" table
                        checkPoliceTable(uid)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showToast("Database error: ${databaseError.message}")
                }
            })
    }

    // Check user role in the "police" table
    private fun checkPoliceTable(uid: String) {
        database.child("police").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists in "police" table, redirect to PoliceActivity
                        redirectToPoliceActivity()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showToast("Database error: ${databaseError.message}")
                }
            })
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this@ReportActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToPoliceActivity() {
        val intent = Intent(this@ReportActivity, PoliceActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

}
