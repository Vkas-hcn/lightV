package com.light.lightV.green

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.light.lightV.BuildConfig


var globalUmpEnableAd = true

fun dealU(context: Activity, action: (() -> Unit)? = null) {
    val params = ConsentRequestParameters.Builder().build()
    val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
    val good = ConsentInformation.OnConsentInfoUpdateSuccessListener {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(context) {
            globalUmpEnableAd = consentInformation.canRequestAds()
            action?.invoke()
        }
    }
    val bad = ConsentInformation.OnConsentInfoUpdateFailureListener {
        globalUmpEnableAd = consentInformation.canRequestAds()
        action?.invoke()
    }
    consentInformation.requestConsentInfoUpdate(context, params, good, bad)
}


private val remoteConfig by lazy { FirebaseRemoteConfig.getInstance() }
fun setupFireCore() {
    if (BuildConfig.DEBUG) return
    FirebaseApp.initializeApp(RedApp.redApp)
    remoteConfig.getString("under").putKv("under")
    remoteConfig.getString("often").putKv("often")
    getFirebaseSomething()
}

fun getFirebaseSomething() {
    remoteConfig.fetchAndActivate().addOnCompleteListener {
        if (it.isSuccessful) {
            remoteConfig.getString("under").putKv("under")
            remoteConfig.getString("often").putKv("often")
        } else
            getFirebaseSomething()
    }
}

