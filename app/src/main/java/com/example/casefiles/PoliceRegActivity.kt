package com.example.casefiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PoliceRegActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextServiceNumber: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var editTextStation: EditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewLogin: TextView
    private lateinit var buttonBack: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_reg)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        editTextEmail = findViewById(R.id.email)
        editTextServiceNumber = findViewById(R.id.serviceNumber)
        editTextPassword = findViewById(R.id.password)
        editTextName = findViewById(R.id.name)
        editTextMobile = findViewById(R.id.number)
        editTextStation = findViewById(R.id.Station)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textViewLogin = findViewById(R.id.Login)
        buttonBack = findViewById(R.id.backButton)
        buttonBack = findViewById(R.id.backButton)

        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString()
            val serviceNumber = editTextServiceNumber.text.toString()
            val password = editTextPassword.text.toString()
            val name = editTextName.text.toString()
            val phoneNumber = editTextMobile.text.toString()
            val station = editTextStation.text.toString()

            if (email.isEmpty() || serviceNumber.isEmpty() || name.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || station.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, name, serviceNumber, phoneNumber, station)
            }
        }

        buttonBack.setOnClickListener{
            val intent = Intent(this, OptionActivity::class.java)
            startActivity(intent)


        }
    }

    private fun registerUser(email: String, password: String, name: String, serviceNumber: String, phoneNumber: String, station: String) {
        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Send email verification
                                user.sendEmailVerification()
                                    .addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            // Save user details in the Realtime Database
                                            saveUserDetails(name, serviceNumber, phoneNumber, station)
                                            Toast.makeText(
                                                this@PoliceRegActivity,
                                                "Registration Successful. Verification email sent. Please check your email.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this@PoliceRegActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(
                                                this@PoliceRegActivity,
                                                "Failed to send verification email. Try again later.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@PoliceRegActivity, "Failed to update user profile.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    progressBar.visibility = View.GONE
                    val error = task.exception
                    if (error != null) {
                        Log.e("PoliceRegActivity", "Registration failed: ${error.message}")
                        Toast.makeText(this@PoliceRegActivity, "Registration failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PoliceRegActivity, "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserDetails(name: String, serviceNumber: String, phoneNumber: String, station: String) {
        val policeDetails = PoliceDetails(name, serviceNumber, phoneNumber, station)
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userRef = databaseReference.child("police").child(uid)
            userRef.setValue(policeDetails)
        }
    }
}