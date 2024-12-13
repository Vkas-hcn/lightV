package com.light.lightV.red

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.light.lightV.BuildConfig
import com.light.lightV.R
import com.light.lightV.databinding.ActivityBlueBinding

class BlueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            closeClick.setOnClickListener { finish() }
            val webSettings = webUi.settings
            webSettings.javaScriptEnabled = true
            imageUi.setImageResource(R.mipmap.logo)
            webUi.webViewClient = WebViewClient()
            webUi.loadUrl("https://sites.google.com/view/sunnyvpn-app/home")
        }
    }
}