package com.light.lightV.red

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.google.android.gms.ads.nativead.NativeAd
import com.light.lightV.R
import com.light.lightV.blue.MobState
import com.light.lightV.blue.RedMob
import com.light.lightV.blue.ggggg.AdUtils
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.blue.updateLimit
import com.light.lightV.databinding.ActivityYellowBinding
import com.light.lightV.green.RedApp.Companion.vcurrentSelectSeverIsSmart
import com.light.lightV.green.getIntFromString
import com.light.lightV.green.isLimit
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialA
import com.light.lightV.green.mobInterstitialC
import com.light.lightV.green.mobNativeB
import com.light.lightV.indigo.currentSelectSever
import com.light.lightV.orange.globalSpecialLaunchAgainB
import com.light.lightV.orange.globalSpecialWaitTimeB
import com.light.lightV.orange.specialLaunchAgainA
import com.light.lightV.purple.RedDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


var resultGlobalAction = ""
var globalCanUpdateResultNative = true

class YellowActivity : AppCompatActivity(), ShadowsocksConnection.Callback {

     lateinit var binding: ActivityYellowBinding
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var seconds = 0
    private val connection = ShadowsocksConnection(true)
    private var backJob: Job? = null
    private var showEndJob: Job? = null

    override fun onStart() {
        super.onStart()
        connection.bandwidthTimeout = 500
    }

    override fun onResume() {
        super.onResume()
        if (globalCanUpdateResultNative) {
            globalCanUpdateResultNative = false
            showHomeAd()
        }
    }

    override fun onStop() {
        connection.bandwidthTimeout = 0
        super.onStop()
    }

    private fun disLoading() {
        RedDialog(show = true).show(supportFragmentManager, "RedDialog")
    }

    fun dismissLoading() {
        (supportFragmentManager.findFragmentByTag("RedDialog") as? DialogFragment)?.dismiss()
    }

    private fun showBackAd(nextFun: () -> Unit) {
        backJob?.cancel()
        backJob = null
        val baseAd = BaseAd.getBackEndInstance()
        backJob = lifecycleScope.launch(Dispatchers.Main) {
            if (baseAd.canShowAd(this@YellowActivity, baseAd) == 0) {
                nextFun()
                return@launch
            }
            if (baseAd.appAdDataForest == null) {
                baseAd.advertisementLoadingForest(this@YellowActivity)
            }
            val startTime = System.currentTimeMillis()
            var elapsedTime: Long
            binding.showLoad = true
            try {
                while (isActive) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    if (isActive && elapsedTime >= 5000L) {
                        nextFun()
                        break
                    }
                    if (elapsedTime >= 1000L && baseAd.canShowAd(
                            this@YellowActivity,
                            baseAd
                        ) == 2
                    ) {
                        backJob?.cancel()
                        backJob = null
                        binding.showLoad = false
                        baseAd.playIntAdvertisementForest(
                            this@YellowActivity,
                            baseAd,
                            closeWindowFun = {
                                nextFun()
                            })
                    }
                    delay(500L)
                }
            } catch (e: Exception) {
                nextFun()
            }
        }
    }

    private fun isActivityExistInStack(context: Context): Boolean {

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(Int.MAX_VALUE)

        val currentClassName = this::class.java.name

        // 遍历任务栈，检查是否存在相同的 Activity
        for (task in runningTasks) {
            if (task.baseActivity?.className == currentClassName) {
                return true
            }
        }
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackAd {
                    finish()
                }
            }
        })
        binding = ActivityYellowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        globalCanUpdateResultNative = true


        val state = intent?.getStringExtra("state")
        val outSecond = intent?.getIntExtra("second", 0) ?: 1
        BaseAd.getBackEndInstance().advertisementLoadingForest(this)
        connection.connect(this, this)

        with(binding) {
            imageBack.setOnClickListener {
                showBackAd {
                    finish()
                }
            }
            if (currentSelectSever == null || vcurrentSelectSeverIsSmart) {
                imageSever.setImageResource(R.mipmap.smart_sever_logo)
            } else {
                imageSever.setImageResource(getIntFromString(currentSelectSever!!.countryCode))
            }

            if (state == "connected") {
                click1.isVisible = false
                connectSpeed.root.isVisible = true
                seconds = outSecond
                timeDisplay.setTextColor(Color.parseColor("#14C41B"))
                connectionState.text = "Connection succeed"
                startCount()
            } else {
                click1.isVisible = true
                connectSpeed.root.isVisible = false
                click1.text = "Fast Node"
                click1.setOnClickListener {
                    resultGlobalAction = "Fast Node"
                    finish()
                }
                val hours = outSecond / 3600
                val minutes = (outSecond % 3600) / 60
                val secs = outSecond % 60
                val time = String.format("%02d:%02d:%02d", hours, minutes, secs)
                timeDisplay.setTextColor(Color.parseColor("#4D3D21"))
                connectionState.text = "Disconnection succeed"
                timeDisplay.text = time
            }
        }
        specialLaunchAgainA(this@YellowActivity)
    }

    private fun showHomeAd() {
        showEndJob?.cancel()
        showEndJob = null
        val baseAd = BaseAd.getEndInstance()
        if (!AdUtils.isVPNConnected()) {
            binding.imgOcAd.isVisible = true
            return
        }
        binding.adLayout.isVisible = true
        binding.imgOcAd.isVisible = true
        baseAd.advertisementLoadingForest(this)
        if (baseAd.canShowAd(this, baseAd) == 0) {
            showEndJob?.cancel()
            showEndJob = null
            return
        }
        showEndJob = lifecycleScope.launch {
            while (isActive) {
                delay(500L)
                if (baseAd.canShowAd(this@YellowActivity, baseAd) == 2) {
                    baseAd.playNativeEnd(this@YellowActivity, baseAd)
                    showEndJob?.cancel()
                    showEndJob = null
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCount()
    }

    private fun stopCount() {
        if (handler != null && runnable != null) {
            handler?.removeCallbacks(runnable!!)
            handler = null
            runnable = null
        }
        seconds = 0
    }

    private fun startCount() {
        if (handler != null && runnable != null) {
            handler?.removeCallbacks(runnable!!)
            handler = null
            runnable = null
        }
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                seconds++
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                val time = String.format("%02d:%02d:%02d", hours, minutes, secs)
                binding.timeDisplay.setText(time)
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnable!!)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
    }

    override fun onServiceConnected(service: IShadowsocksService) {
    }

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        binding.connectSpeed.downloadData.text =
            String.format("%s", Formatter.formatFileSize(this@YellowActivity, stats.rxRate))
        binding.connectSpeed.uploadData.text =
            String.format("%s", Formatter.formatFileSize(this@YellowActivity, stats.txRate))
    }
}