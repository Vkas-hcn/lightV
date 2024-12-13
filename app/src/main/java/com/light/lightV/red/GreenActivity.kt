package com.light.lightV.red

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.shadowsocks.bg.BaseService
import com.light.lightV.R
import com.light.lightV.blue.MobState
import com.light.lightV.blue.RedMob
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.blue.updateLimit
import com.light.lightV.databinding.ActivityGreenBinding
import com.light.lightV.green.RedApp
import com.light.lightV.green.isLimit
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialB
import com.light.lightV.indigo.appSeverLists
import com.light.lightV.indigo.changeSever
import com.light.lightV.indigo.currentSelectSever
import com.light.lightV.indigo.olderServer
import com.light.lightV.orange.globalConnectState
import com.light.lightV.orange.globalSpecialLaunchAgainB
import com.light.lightV.orange.globalSpecialWaitTimeB
import com.light.lightV.purple.GreenDialog
import com.light.lightV.purple.RedDialog
import com.light.lightV.yellow.YellowAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var globalSwitchSever = ""

class GreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGreenBinding
    private var backJob: Job? = null
    private val yellowAdapter: YellowAdapter by lazy {
        YellowAdapter()
    }

    private fun disLoading() {
        RedDialog(show = true).show(supportFragmentManager, "RedDialog")
    }

    fun dismissLoading() {
        (supportFragmentManager.findFragmentByTag("RedDialog") as? DialogFragment)?.dismiss()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackAd { finish() }
            }
        })
        BaseAd.getBackListInstance().advertisementLoadingForest(this)
        with(binding) {
            imageBack.setOnClickListener {
                showBackAd { finish() }
            }

            updateSmartPart()

            yellowAdapter.yellowUpdateAll = {
                if (globalConnectState == BaseService.State.Connected) {
                    if (RedApp.vcurrentSelectSeverIsSmart || it.ip != currentSelectSever?.ip) {
                        GreenDialog().apply {
                            okAction = {
                                changeSever(it, false)
                                updateSmartPart()
                                yellowAdapter.notifyDataSetChanged()
                                globalSwitchSever = "disConnect"
                                finish()
                            }
                        }.show(supportFragmentManager, "GreenDialog")
                    }
                } else {
                    changeSever(it, false)
                    updateSmartPart()
                    yellowAdapter.notifyDataSetChanged()
                    globalSwitchSever = "connect"
                    finish()
                }
            }
            with(recyclerView) {
                adapter = yellowAdapter
                layoutManager = LinearLayoutManager(context)
            }

            if (olderServer.isNullOrEmpty()) {
                recyclerView.isVisible = false
                widgetSmart.root.isVisible = false
                textEmpty.isVisible = true
            } else {
                recyclerView.isVisible = true
                widgetSmart.root.isVisible = true
                textEmpty.isVisible = false
                yellowAdapter.dataList = olderServer
                yellowAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateSmartPart() {
        with(binding) {
            with(widgetSmart) {
                smartSelect.setImageResource(if (RedApp.vcurrentSelectSeverIsSmart) R.mipmap.sever_select_logo else R.mipmap.sever_no_select_logo)
                root.setOnClickListener {
                    if (globalConnectState == BaseService.State.Connected) {
                        if (RedApp.vcurrentSelectSeverIsSmart) return@setOnClickListener
                        GreenDialog().apply {
                            okAction = {
                                changeSever(appSeverLists?.data?.smartList?.random(), true)
                                updateSmartPart()
                                yellowAdapter.notifyDataSetChanged()
                                globalSwitchSever = "disConnect"
                                finish()
                            }
                        }.show(supportFragmentManager, "GreenDialog")
                    } else {
                        changeSever(appSeverLists?.data?.smartList?.random(), true)
                        smartSelect.setImageResource(R.mipmap.sever_select_logo)
                        yellowAdapter.notifyDataSetChanged()
                        globalSwitchSever = "connect"
                        finish()
                    }
                }
            }
        }
    }

    private fun showBackAd(nextFun: () -> Unit) {
        backJob?.cancel()
        backJob = null
        val baseAd = BaseAd.getBackListInstance()
        backJob = lifecycleScope.launch(Dispatchers.Main) {
            if (baseAd.canShowAd(this@GreenActivity, baseAd) == 0) {
                nextFun()
                return@launch
            }
            if (baseAd.appAdDataForest == null) {
                baseAd.advertisementLoadingForest(this@GreenActivity)
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
                    if (elapsedTime >= 1000L && baseAd.canShowAd(this@GreenActivity, baseAd) == 2) {
                        backJob?.cancel()
                        backJob = null
                        binding.showLoad = false
                        baseAd.playIntAdvertisementForest(
                            this@GreenActivity,
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

}