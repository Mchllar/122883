package com.example.casefiles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var buttonLogout: Button
    private lateinit var viewProfile: Button
    private lateinit var buttonReportIncident: Button
    private lateinit var buttonViewUpdates: Button
    private lateinit var buttonViewReports: Button


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        currentUser = auth.currentUser

        buttonLogout = findViewById(R.id.logout)
        viewProfile = findViewById(R.id.viewProfile)
        buttonReportIncident = findViewById(R.id.reportIncident)
        buttonViewUpdates = findViewById(R.id.viewUpdates)
        buttonViewReports = findViewById(R.id.viewReport)



        if(currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        buttonLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

        viewProfile.setOnClickListener{
            val intent = Intent(this, ViewProfileActivity::class.java)
            startActivity(intent)
            finish()

        }

        buttonReportIncident.setOnClickListener{
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            finish()

        }

        buttonViewUpdates.setOnClickListener{
            val intent = Intent(this, CheckUpdatesActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonViewReports.setOnClickListener{
            val intent = Intent(this, ViewReportsActivity::class.java)
            startActivity(intent)
            finish()


        }



    }
}