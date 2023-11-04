package com.example.casefiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextNationalID: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var editTextResidence: EditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("civilian")

        editTextEmail = findViewById(R.id.email)
        editTextNationalID = findViewById(R.id.NationalID)
        editTextPassword = findViewById(R.id.password)
        editTextName = findViewById(R.id.name)
        editTextMobile = findViewById(R.id.number)
        editTextResidence = findViewById(R.id.residence)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)

        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString()
            val nationalId = editTextNationalID.text.toString()
            val password = editTextPassword.text.toString()
            val name = editTextName.text.toString()
            val phoneNumber = editTextMobile.text.toString()
            val residence = editTextResidence.text.toString()

            if (email.isEmpty() || nationalId.isEmpty() || name.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || residence.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, name, nationalId, phoneNumber, residence)
            }
        }

    }

    private fun registerUser(email: String, password: String, name: String, nationalId: String, phoneNumber: String, residence: String) {
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
                                // Update the user's display name

                                // Send email verification
                                user.sendEmailVerification()
                                    .addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            // Email verification sent successfully
                                            // Save user details in the Realtime Database
                                            saveUserDetails(name, nationalId, phoneNumber, residence)
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Registration Successful. Verification email sent. Please check your email.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            // Failed to send verification email
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Failed to send verification email. Try again later.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                // Failed to update user profile
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@RegisterActivity, "Failed to update user profile.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Registration failed
                    progressBar.visibility = View.GONE

                    val error = task.exception
                    if (error != null) {
                        Log.e("LoginActivity", "Authentication failed: ${error.message}")
                    }
                    Toast.makeText(this@RegisterActivity, "Registration Failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDetails(name: String, nationalId: String, phoneNumber: String, residence: String) {
        val userDetails = UserDetails(name, nationalId, phoneNumber, residence)
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userRef = databaseReference.child(uid)
            userRef.setValue(userDetails)
        }
    }

}
