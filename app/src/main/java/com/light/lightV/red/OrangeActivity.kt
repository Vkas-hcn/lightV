package com.light.lightV.red

import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.github.shadowsocks.bg.BaseService
import com.light.lightV.R
import com.light.lightV.blue.MobState
import com.light.lightV.blue.RedMob
import com.light.lightV.databinding.ActivityOrangeBinding
import com.light.lightV.green.addressLimit
import com.light.lightV.green.getKv
import com.light.lightV.green.isLimit
import com.light.lightV.green.isNetworkAvailable
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialC
import com.light.lightV.green.putKv
import com.light.lightV.green.toNow
import com.light.lightV.indigo.OrangeVM
import com.light.lightV.indigo.sendPoint
import com.light.lightV.orange.globalConnectState
import com.light.lightV.orange.globalSpecialLaunchAgainB
import com.light.lightV.orange.globalSpecialWaitTimeB
import com.light.lightV.orange.specialLaunchAgainB
import com.light.lightV.purple.OrangeDialog
import com.light.lightV.purple.RedDialog
import com.light.lightV.purple.YellowDialog
import com.light.lightV.yellow.RedAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


var showGuideBlack = true

class OrangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrangeBinding

    private val vm: OrangeVM by viewModels()

    private val redAdapter: RedAdapter by lazy {
        RedAdapter(this@OrangeActivity.supportFragmentManager, this@OrangeActivity.lifecycle)
    }

    var clickBottomTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!vm.canBack) return
                if (showGuideBlack) {
                    updateMask(connect = false)
                } else {
                    moveTaskToBack(true)
                }
            }
        })
        with(vm) {
            canSwitchTab.observe(this@OrangeActivity) {
                binding.fatherHome.isEnabled = it
                binding.fatherProxy.isEnabled = it
                binding.fatherSet.isEnabled = it
            }
            updateNoVpnPackages.observe(this@OrangeActivity) {
                changePage(0)
                binding.mainPageFather.currentItem = 0
                afterSwitchTabUpdateNoVpnP.postValue(true)
            }
        }
        with(binding) {
            mainPageFather.adapter = redAdapter
            mainPageFather.isUserInputEnabled = false

            fatherHome.setOnClickListener {
                updateClickTabTime({
                    changePage(0)
                    mainPageFather.currentItem = 0
                })
            }
            fatherProxy.setOnClickListener {
                updateClickTabTime({
                    changePage(1)
                    mainPageFather.currentItem = 1
                })
            }
            fatherSet.setOnClickListener {
                updateClickTabTime({
                    changePage(2)
                    mainPageFather.currentItem = 2
                })
            }

//            clickBlack.isVisible = showGuideBlack
//            clickBlack.setOnClickListener { updateMask() }
        }
        if (!isNetworkAvailable(this)) {
            YellowDialog().show(supportFragmentManager, "YellowDialog")
        } else if (addressLimit) {
            OrangeDialog().show(supportFragmentManager, "OrangeDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        sendPoint("home_sun", "userCode".getKv())
        when (globalSwitchSever) {
            "connect" -> {
                changePage(0)
                binding.mainPageFather.currentItem = 0
                vm.switchSever(true)
            }

            "disConnect" -> {
                changePage(0)
                binding.mainPageFather.currentItem = 0
                vm.switchSever(false)
            }

            else -> {}
        }
        globalSwitchSever = ""
        specialLaunchAgainB(this@OrangeActivity)
    }

    private fun updateMask(connect: Boolean = true) {
        if (connect) {
            vm.doHandConnect()
        }
        showGuideBlack = false
//        binding.clickBlack.isVisible = showGuideBlack
    }

    private fun changePage(index: Int) {
        with(binding) {
            imageHome.setImageResource(if (index == 0) R.mipmap.home_main_light_logo else R.mipmap.home_main_logo)
            imageProxy.setImageResource(if (index == 1) R.mipmap.home_proxy_light_logo else R.mipmap.home_proxy_logo)
            imageSet.setImageResource(if (index == 2) R.mipmap.home_set_light_logo else R.mipmap.home_set_logo)
            textHome.setTextColor(Color.parseColor(if (index == 0) "#FFC24B" else "#FFFFFF"))
            textProxy.setTextColor(Color.parseColor(if (index == 1) "#FFC24B" else "#FFFFFF"))
            textSet.setTextColor(Color.parseColor(if (index == 2) "#FFC24B" else "#FFFFFF"))
        }
    }

    private fun updateClickTabTime(action: (() -> Unit)? = null) {
        if (mobInterstitialC.isBlock || isLimit) {
            action?.invoke()
            return
        }
        clickBottomTime += 1
        if (clickBottomTime >= 2 && globalConnectState == BaseService.State.Connected) {
            clickBottomTime = 0
            if (mobInterstitialC.mobState != MobState.GetSuccess) {
                action?.invoke()
            } else {
//                showBack(action)
            }
        } else {
            action?.invoke()
        }
    }

    private fun disLoading() {
        RedDialog(show = true).show(supportFragmentManager, "RedDialog")
    }

    fun dismissLoading() {
        (supportFragmentManager.findFragmentByTag("RedDialog") as? DialogFragment)?.dismiss()
    }

    private fun showBack(action: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = withTimeoutOrNull(1000L) {
                while (true) {
                    if (mobInterstitialC.mobState == MobState.GetSuccess) {
                        withContext(Dispatchers.Main) {
                            disLoading()
                            delay(800)
                            mobInterstitialC.stateAction = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    while (lifecycle.currentState != Lifecycle.State.RESUMED) {
                                        if (!noAllowLaunchAgain) noAllowLaunchAgain = true
                                        globalSpecialWaitTimeB += 100
                                        if (globalSpecialWaitTimeB >= 3000L && !globalSpecialLaunchAgainB) {
                                            globalSpecialLaunchAgainB = true
                                        }
                                        delay(100)
                                    }
                                    dismissLoading()
                                    action?.invoke()
                                }
                            }
                            "mobInterstitialC current state  ${lifecycle.currentState}".lightVDebugLog()
                            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                                RedMob.showAd(mobInterstitialC, this@OrangeActivity)
                            }
                        }
                        break
                    }
                }
            }
            if (result == null) {
                CoroutineScope(Dispatchers.Main).launch {
                    while (lifecycle.currentState != Lifecycle.State.RESUMED) {
                        delay(100)
                    }
                    dismissLoading()
                    action?.invoke()
                }
            }
        }
    }
}