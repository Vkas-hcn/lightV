package com.light.lightV.red

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdActivity
import com.light.lightV.green.lightVDebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

var noAllowLaunchAgain = false
var clondAd = false
var isInBackground = false
var lastBackgroundTime: Long = 0

class AppLifeMaster : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    var adActivity: Activity? = null
    private fun restartApp(activity: Activity) {
        if (adActivity != null) {
            clondAd = true
            adActivity?.finish()
        }
        val intent = Intent(activity, RedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        "onEnterBackground".lightVDebugLog()

        lastBackgroundTime = System.currentTimeMillis()
        isInBackground = true
    }
    override fun onActivityPaused(activity: Activity) {
        "onActivityPaused ${activity::class.java}".lightVDebugLog()
    }

    override fun onActivityResumed(activity: Activity) {
        if (isInBackground) {
            "onActivityResumed ${activity::class.java}".lightVDebugLog()

            isInBackground = false
            val currentTime = System.currentTimeMillis()
            val backgroundDuration = currentTime - lastBackgroundTime
            if (backgroundDuration > 3000) {
                "backgroundDuration ${activity::class.java}".lightVDebugLog()
                restartApp(activity)
            }
        }
        if (noAllowLaunchAgain) noAllowLaunchAgain = false
    }

    override fun onActivityStopped(activity: Activity) {

    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        "onActivityCreated ${activity::class.java}".lightVDebugLog()
    }

    override fun onActivityStarted(activity: Activity) {
        "onActivityStarted ${activity::class.java}".lightVDebugLog()
        if (activity is AdActivity) {
            adActivity = activity
        }
    }


    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        "onActivitySaveInstanceState ${activity::class.java}".lightVDebugLog()
    }

    override fun onActivityDestroyed(activity: Activity) {
        "onActivityDestroyed ${activity::class.java}".lightVDebugLog()
    }
}