package com.light.lightV.green

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.shadowsocks.Core
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.indigo.getPackages
import com.light.lightV.indigo.loadAdminType
import com.light.lightV.red.AppLifeMaster
import com.light.lightV.red.RedActivity
import com.tencent.mmkv.MMKV

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
            BaseAd.getOpenInstance().isAppOpenSameDayBa()
            getPackages()
            netLimit()
            loadAdminType()
        }
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
    }
}