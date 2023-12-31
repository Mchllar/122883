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
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextNationalID: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var editTextResidence: EditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewLogin: TextView
    private lateinit var buttonBack: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

//    override fun onStart() {
//        super.onStart()
//        auth = FirebaseAuth.getInstance()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        editTextEmail = findViewById(R.id.email)
        editTextNationalID = findViewById(R.id.NationalID)
        editTextPassword = findViewById(R.id.password)
        editTextName = findViewById(R.id.name)
        editTextMobile = findViewById(R.id.number)
        editTextResidence = findViewById(R.id.residence)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textViewLogin = findViewById(R.id.Login)
        buttonBack = findViewById(R.id.backButton)

        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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

        buttonBack.setOnClickListener{
            val intent = Intent(this, OptionActivity::class.java)
            startActivity(intent)


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
                                // Send email verification
                                user.sendEmailVerification()
                                    .addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
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
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Failed to send verification email. Try again later.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@RegisterActivity, "Failed to update user profile.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    progressBar.visibility = View.GONE
                    val error = task.exception
                    if (error != null) {
                        Log.e("RegisterActivity", "Registration failed: ${error.message}")
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserDetails(name: String, nationalId: String, phoneNumber: String, residence: String) {
        val userDetails = UserDetails(name, nationalId, phoneNumber, residence)
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userRef = databaseReference.child("users").child(uid)
            userRef.setValue(userDetails)
        }
    }
}
