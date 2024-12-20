package com.light.lightV.green

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.shadowsocks.Core
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.light.lightV.blue.ggggg.AdUtils
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.indigo.getPackages
import com.light.lightV.indigo.loadAdminType
import com.light.lightV.red.AppLifeMaster
import com.light.lightV.red.RedActivity
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class RedApp : Application() {

    override fun onCreate() {
        super.onCreate()
        redApp = this
        MMKV.initialize(this)
        Core.init(this, RedActivity::class)
        if (this.topIs()) {
            "init time".lightVDebugLog()
            MobileAds.initialize(this)
            Firebase.initialize(this)
            FirebaseApp.initializeApp(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifeMaster())
            registerActivityLifecycleCallbacks(AppLifeMaster())
            setAndroidId()
            setGid()
            BaseAd.getOpenInstance().isAppOpenSameDayBa()
            getPackages()
            netLimit()
            loadAdminType()
            AdUtils.haveRefDataChangingBean(this)
        }
    }

    fun getUserDId() {
        "userCode".getKv().let {
            if (it.isBlank()) {
                val num = generateRandomFourDigitNumber().toString()
                num.putKv("userCode")
                AdUtils.log("userCode1=====${num}")
                AdUtils.log("userCode2=====${it}")
            }
        }
    }

    fun generateRandomFourDigitNumber(): Int {
        // 生成1000到9999之间的随机数
        return Random.nextInt(1000, 10000)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Core.updateNotificationChannels()
    }

    fun Context.topIs(): Boolean {
        val pid = android.os.Process.myPid()
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in am.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return this.packageName == processInfo.processName
            }
        }
        return false
    }


    companion object {
        lateinit var redApp: Application
        var vcurrentSelectSeverIsSmart = false
        var vcurrentSelectSeverIsSmart2222 = true
    }


    private fun setAndroidId(){
        val data = AdUtils.andoridIdTba.getKv()
        if (data.isBlank()) {
            UUID.randomUUID().toString().putKv(AdUtils.andoridIdTba)
        }
    }

    private fun setGid(){
        CoroutineScope(Dispatchers.IO).launch {
            val adId = runCatching {
                AdvertisingIdClient.getAdvertisingIdInfo(redApp).id
            }.getOrNull() ?: ""
            Log.d("AdId", "Google Advertising ID: $adId")
            adId.putKv(AdUtils.gIdTba)

        }
    }
}