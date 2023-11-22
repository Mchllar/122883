package com.example.casefiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class PoliceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var buttonViewProfile: Button
    private lateinit var buttonViewIncidents: Button
    private lateinit var buttonMakeUpdates: Button
    private lateinit var buttonReportIncident: Button
    private lateinit var buttonViewDashboard: Button
    private lateinit var buttonLogout: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police)

        buttonViewProfile = findViewById(R.id.viewProfile)
        buttonViewIncidents = findViewById(R.id.viewIncidents)
        buttonMakeUpdates = findViewById(R.id.makeUpdates)
        buttonReportIncident = findViewById(R.id.reportIncident)
        buttonViewDashboard = findViewById(R.id.viewDashboard)
        buttonLogout = findViewById(R.id.logout)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        currentUser = auth.currentUser

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

        buttonViewProfile.setOnClickListener{
            val intent = Intent(this, PoliceProfileActivity::class.java)
            startActivity(intent)
            finish()

        }

        buttonReportIncident.setOnClickListener{
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
            finish()

        }

        buttonViewIncidents.setOnClickListener{
            val intent = Intent(this, ViewHistoryIncidentsActivity::class.java)
            startActivity(intent)
            finish()

        }

        buttonMakeUpdates.setOnClickListener{
            val intent = Intent(this, CreateUpdatesActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonViewDashboard.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}
