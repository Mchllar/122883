package com.example.casefiles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PoliceProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var resetPasswordButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var buttonBack: Button
    private lateinit var buttonAccDel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_profile)

        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        database = FirebaseDatabase.getInstance().reference

        val userNameTextView = findViewById<TextView>(R.id.user_name)
        val userResidenceTextView = findViewById<TextView>(R.id.user_station)
        val userServiceNumberTextView = findViewById<TextView>(R.id.user_service_number)
        val userPhoneNumberTextView = findViewById<TextView>(R.id.user_phone_number)
        buttonBack = findViewById(R.id.backButton)
        buttonAccDel = findViewById(R.id.deleteButton)

        buttonBack.setOnClickListener {
            val intent = Intent(this, PoliceActivity::class.java)
            startActivity(intent)
        }

        buttonAccDel.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Call the methods to retrieve user data
        getUserName(userNameTextView)
        getUserStation(userResidenceTextView)
        getUserServiceNumber(userServiceNumberTextView)
        getUserPhoneNumber(userPhoneNumberTextView)

        // Check if a user is signed in
        if (currentUser != null) {
            // Display user's profile information
            userNameTextView.text = currentUser.displayName ?: getString(R.string.default_user_name)
        }

        // Initialize the reset password button
        resetPasswordButton = findViewById(R.id.reset_password_button)
        resetPasswordButton.setOnClickListener {
            // Handle the password reset process when the button is clicked
            resetUserPassword()
        }
    }

    private fun resetUserPassword() {
        val userEmail = auth.currentUser?.email ?: ""

        auth.sendPasswordResetEmail(userEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    // Provide feedback to the user
                    val successMessage = getString(R.string.password_reset_success, userEmail)
                    Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                } else {
                    // Password reset email sending failed
                    // Handle the error and provide feedback to the user
                    val errorMessage = getString(R.string.password_reset_failure, task.exception?.message)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showDeleteConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Account")
        alertDialogBuilder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            // User clicked Yes, delete the account
            deleteAccount()
        }

        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            // User clicked No, dismiss the dialog
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun deleteAccount() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Account deleted successfully
                    Toast.makeText(this@PoliceProfileActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    // Redirect to login or any other appropriate screen
                    val intent = Intent(this@PoliceProfileActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If the delete fails, display a message to the user.
                    Toast.makeText(this@PoliceProfileActivity, "Failed to delete account. Please try again later.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun getUserName(userNameTextView: TextView) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val userRef = database.child("police").child(uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").value.toString()
                        val nameText = getString(R.string.user_name_label, name)
                        userNameTextView.text = nameText
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    val errorMessage = getString(R.string.database_error, databaseError.message)
                    Toast.makeText(this@PoliceProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getUserStation(userResidenceTextView: TextView) {
        // Get the user's residence from the Realtime Database based on their UID
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val userRef = database.child("police").child(uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val station = dataSnapshot.child("station").value.toString()
                        val stationText = getString(R.string.user_station_label, station)
                        userResidenceTextView.text = stationText
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    val errorMessage = getString(R.string.database_error, databaseError.message)
                    Toast.makeText(this@PoliceProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getUserServiceNumber(userNationalIdTextView: TextView) {
        // Get the user's national ID from the Realtime Database based on their UID
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val userRef = database.child("police").child(uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val serviceNumber = dataSnapshot.child("serviceNumber").value.toString()
                        val serviceNumberText = getString(R.string.user_service_number_label, serviceNumber)
                        userNationalIdTextView.text = serviceNumberText
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    val errorMessage = getString(R.string.database_error, databaseError.message)
                    Toast.makeText(this@PoliceProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getUserPhoneNumber(userPhoneNumberTextView: TextView) {
        // Get the user's phone number from the Realtime Database based on their UID
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val userRef = database.child("police").child(uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val phoneNumber = dataSnapshot.child("phoneNumber").value.toString()
                        val phoneNumberText = getString(R.string.user_phone_number_label, phoneNumber)
                        userPhoneNumberTextView.text = phoneNumberText
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    val errorMessage = getString(R.string.database_error, databaseError.message)
                    Toast.makeText(this@PoliceProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}
