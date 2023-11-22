package com.example.casefiles

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize FirebaseAuth and DatabaseReference
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize UI elements
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        textViewRegister = findViewById(R.id.register)
        progressBar = findViewById(R.id.progressBar)

        // Navigate to the registration screen
        textViewRegister.setOnClickListener {
            val intent = Intent(this, OptionActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Resend email verification
        val buttonResendVerification = findViewById<Button>(R.id.btn_resend_verification)
        buttonResendVerification.setOnClickListener {
            val user = auth.currentUser
            user?.sendEmailVerification()
            Toast.makeText(baseContext, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show()
        }

        // Login button click listener
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(baseContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.INVISIBLE

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        if (user != null) {
                            if (user.isEmailVerified) {
                                val uid = user.uid
                                // Check user role in the database
                                checkUserRole(uid)
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "Email is not verified. Please check your email for a verification link.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(baseContext, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val error = task.exception
                        if (error != null) {
                            Toast.makeText(
                                baseContext,
                                "Authentication failed: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }


    }

    // Check user role in the database
    private fun checkUserRole(uid: String) {
        databaseReference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists in "users" table, redirect to MainActivity
                        redirectToMainActivity()
                        showToast("Login Successful.")
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
        databaseReference.child("police").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists in "police" table, redirect to PoliceActivity
                        redirectToPoliceActivity()
                        showToast("Login Successful.")
                    } else {
                        showToast("User not found in the database.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showToast("Database error: ${databaseError.message}")
                }
            })
    }

    // Redirect to MainActivity
    private fun redirectToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Redirect to PoliceActivity
    private fun redirectToPoliceActivity() {
        val intent = Intent(this@LoginActivity, PoliceActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }
}
