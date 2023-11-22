package com.example.casefiles

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        buttonBack = findViewById(R.id.backButton)

        buttonBack.setOnClickListener{
            val intent = Intent(this, PoliceActivity::class.java)
            startActivity(intent)


        }

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        webView.webChromeClient = WebChromeClient()

        val embedUrl = "https://app.powerbi.com/reportEmbed?reportId=<YourReportId>&groupId=<YourGroupId>"
        val embedToken = "<YourEmbedToken>"
        val embedType = "report" // or "dashboard"

        val html = """<html>
                        <head><title>Power BI Embedding</title></head>
                        <body>
                            <div>
<iframe title="Report Section" width="600" height="373.5" src="https://app.powerbi.com/view?r=eyJrIjoiNThlMzc5MjAtZTQ3OS00MTVhLWFlMjUtZjk3MWY0NDQzNWRkIiwidCI6ImE0NjIyOWM3LTIxZDEtNDE3ZC1hMWNiLTE4NTdhMDdkMjc2NSIsImMiOjh9" frameborder="0" allowFullScreen="true"></iframe>                        </body>
                    </html>"""

        webView.loadData(html, "text/html", "utf-8")
    }
}
