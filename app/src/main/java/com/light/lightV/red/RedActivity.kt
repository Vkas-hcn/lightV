package com.light.lightV.red

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.github.shadowsocks.Core
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.light.lightV.blue.MobState
import com.light.lightV.blue.RedMob
import com.light.lightV.blue.ggggg.AdUtils
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.databinding.ActivityRedBinding
import com.light.lightV.green.RedApp.Companion.redApp
import com.light.lightV.green.dealU
import com.light.lightV.green.getKv
import com.light.lightV.green.getRealAdConfig
import com.light.lightV.green.getRealLimitConfig
import com.light.lightV.green.initOrange
import com.light.lightV.green.initRed
import com.light.lightV.green.initYellow
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialA
import com.light.lightV.green.mobNativeA
import com.light.lightV.green.mobOpen
import com.light.lightV.green.putKv
import com.light.lightV.indigo.loadSevers
import com.light.lightV.indigo.sendPoint
import com.light.lightV.orange.globalCanUpdateHomeNative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull


class RedActivity : AppCompatActivity() {
    private var jobOpenAdsForest: Job? = null
    private var startCateForest: Job? = null
    private lateinit var binding: ActivityRedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                "no back".lightVDebugLog()
            }
        })
        updateUserOpinions()
        initTime()
        sendPoint("open_sun", "userCode".getKv())
        AdUtils.getBlackList(this)
        globalCanUpdateHomeNative = true
        globalCanUpdateResultNative = true
        loadSevers()
            initRed(getRealAdConfig())
            initYellow(getRealLimitConfig())
            initOrange()
//            dealU(this@RedActivity) {
//                initAm()
//            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(4000)
                initRed(getRealAdConfig())
                initYellow(getRealLimitConfig())
            }
    }

    private fun initTime() {
        getFileBaseData()
        startCountdown()
    }

    private fun startCountdown() {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 14000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.progressBar.progress = progress
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
            }
        })
        animator.start()
    }
    private fun base64Decode(base64Str: String): String {
        return String(Base64.decode(base64Str, Base64.DEFAULT))
    }
    private fun getFileBaseData() {
        initFaceBook()
        startCateForest = lifecycleScope.launch {
            var isCa = false
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                base64Decode(auth.getString("under")).putKv("under")
                base64Decode(auth.getString("often")).putKv("often")
                Log.e("TAG", "getFileBaseData-under: ${"under".getKv()}")
                Log.e("TAG", "getFileBaseData-often: ${"often".getKv()}")
                isCa = true
                initFaceBook()
            }
            try {
                withTimeout(4000L) {
                    while (true) {
                        if (!isActive) {
                            break
                        }
                        if (isCa) {
                            loadAdFun()
                            cancel()
                            startCateForest = null
                        }
                        delay(500)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                cancel()
                startCateForest = null
                loadAdFun()
            }
        }
    }

    private fun initFaceBook() {
        val bean = AdUtils.getLogicJson().dda ?: ""
        if (bean.isBlank()) {
            return
        }
        Log.e("TAG", "initFaceBook: ${bean}")
        FacebookSdk.setApplicationId(bean)
        FacebookSdk.sdkInitialize(redApp)
        AppEventsLogger.activateApp(redApp)
    }

    private fun loadAdFun() {
        AdUtils.log("loading open")
        BaseAd.getOpenInstance().advertisementLoadingForest(this)
        connectToVPNFun()
        BaseAd.getHomeInstance().advertisementLoadingForest(this)
        BaseAd.getConnectInstance().advertisementLoadingForest(this)
    }

    private fun ccc() {
        if (AdUtils.isVPNConnected()) {
            loadOpenAd()
        } else {
            checkData()
        }
    }

    private fun connectToVPNFun() {
        if (AdUtils.cmpState.getKv() == "1") {
            ccc()
            return
        }
        GlobalScope.launch {
            while (isActive) {
                if (AdUtils.cmpState.getKv()== "1") {
                    ccc()
                    cancel()
                }
                delay(500)
            }
        }
    }

    private fun checkData() {
        jobOpenAdsForest?.cancel()
        jobOpenAdsForest = lifecycleScope.launch {
            delay(1000L)
            try {
                withTimeout(6000L) {
                    var keepLooping = true
                    while (keepLooping && isActive) {
                        val adData = "under".getKv()
                        val blockData = AdUtils.blockData.getKv()
                        if (adData.isBlank() || blockData.isBlank()) {
                            delay(500L)
                        } else {
                            keepLooping = false
                            finishOpenAd()
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                finishOpenAd()
            }
        }
        jobOpenAdsForest?.invokeOnCompletion {
            jobOpenAdsForest = null
        }
    }

    private fun loadOpenAd() {
        jobOpenAdsForest?.cancel()
        jobOpenAdsForest = null
        jobOpenAdsForest = lifecycleScope.launch {
            if (!AdUtils.isVPNConnected() || BaseAd.getOpenInstance().limitIsExceeded()) {
                finishOpenAd()
                return@launch
            }
            try {
                withTimeout(12000L) {
                    while (isActive) {
                        val showState = BaseAd.getOpenInstance()
                            .displayOpenAdvertisementForest(this@RedActivity, fullScreenFun = {
                                finishOpenAd()
                            })
                        if (showState) {
                            cancel()
                            jobOpenAdsForest = null
                            binding.progressBar.progress = 100
                        }
                        delay(500L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                finishOpenAd()
            }
        }
    }

    private fun finishOpenAd() {
        jobOpenAdsForest?.cancel()
        jobOpenAdsForest = null
        binding.progressBar.progress = 100
        navigateToOrangeActivity()
    }
    private fun navigateToOrangeActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            while (lifecycle.currentState != Lifecycle.State.RESUMED) {
                delay(100)
            }
            if(clondAd){
                return@launch
            }
            val intent = Intent(this@RedActivity, OrangeActivity::class.java)
            startActivity(intent)
            clondAd = false
        }
    }

    private fun updateUserOpinions() {
        if (AdUtils.cmpState.getKv() == "1") {
            return
        }
        val debugSettings =
            ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("76A730E9AE68BD60E99DF7B83D65C4B4")
                .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) {
                    if (consentInformation.canRequestAds()) {
                        "1".putKv(AdUtils.cmpState)
                    }
                }
            },
            {
                "1".putKv(AdUtils.cmpState)
            }
        )
    }

}