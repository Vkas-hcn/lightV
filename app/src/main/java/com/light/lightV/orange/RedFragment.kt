package com.light.lightV.orange

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.google.android.gms.ads.nativead.NativeAd
import com.light.lightV.R
import com.light.lightV.blue.MobState
import com.light.lightV.blue.RedMob
import com.light.lightV.blue.ggggg.AdUtils
import com.light.lightV.blue.ggggg.AdUtils.getLogicJson
import com.light.lightV.blue.ggggg.AdUtils.log
import com.light.lightV.blue.ggggg.BaseAd
import com.light.lightV.blue.updateLimit
import com.light.lightV.databinding.FragmentRedBinding
import com.light.lightV.green.addressLimit
import com.light.lightV.green.getIntFromString
import com.light.lightV.green.getKv
import com.light.lightV.green.isLimit
import com.light.lightV.green.isNetworkAvailable
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialA
import com.light.lightV.green.mobInterstitialC
import com.light.lightV.green.mobNativeA
import com.light.lightV.green.mobNativeB
import com.light.lightV.green.putKv
import com.light.lightV.green.toNow
import com.light.lightV.indigo.OrangeVM
import com.light.lightV.indigo.appSeverLists
import com.light.lightV.indigo.changeSever
import com.light.lightV.indigo.currentSelectSever
import com.light.lightV.indigo.isLoadingSever
import com.light.lightV.indigo.loadSevers
import com.light.lightV.indigo.vcurrentSelectSeverIsSmart
import com.light.lightV.purple.OrangeDialog
import com.light.lightV.purple.RedDialog
import com.light.lightV.purple.YellowDialog
import com.light.lightV.red.GreenActivity
import com.light.lightV.red.RedActivity
import com.light.lightV.red.YellowActivity
import com.light.lightV.red.noAllowLaunchAgain
import com.light.lightV.red.resultGlobalAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull


var globalConnectState = BaseService.State.Idle
var globalCanUpdateHomeNative = true

class RedFragment : Fragment(), ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var seconds = 0
    private var showConnectJob: Job? = null
    private var showHomeJob: Job? = null
    private val vm: OrangeVM by activityViewModels()

    lateinit var binding: FragmentRedBinding
    private val connect = registerForActivityResult(StartService()) {}

    private var requestNotificationPermissionLauncher: ActivityResultLauncher<String>? = null

    private fun switchVpnStateF() {
        if (globalConnectState.canStop) {
            showConnectAd(false) {
                Core.stopService()
            }
        } else {
            AdUtils.connectIp = currentSelectSever?.ip.toString()
            AdUtils.connectCity = currentSelectSever?.cityName.toString()
            connect.launch(null)
        }
    }


    private val connection = ShadowsocksConnection(true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRedBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun noMoreClick(canClick: Boolean, isConnect: Boolean) {
        vm.canClickTab(canClick)
        vm.canClick = canClick
        vm.canBack = !isConnect
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lightDate.text = "".toNow()

        requestNotificationPermissionLauncher = registerForActivityResult<String, Boolean>(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                switchVpnStateF()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        with(vm) {
            handConnect.observe(viewLifecycleOwner) {
                startConnectStep()
            }
            switchSeverConnect.observe(viewLifecycleOwner) {
                startConnectStep()
            }
            afterSwitchTabUpdateNoVpnP.observe(viewLifecycleOwner) {
                if (globalConnectState == BaseService.State.Connected) {
                    switchStateWithoutRoute = true
                    switchVpnStateF()
                    resetUiState(false)
                    startConnectStep()
                } else {
                    startConnectStep()
                }
            }
        }
        with(binding) {
            connectButton.setOnClickListener {
                if (!vm.canClick) {
                    Toast.makeText(
                        requireActivity(),
                        "Please try again later.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@setOnClickListener
                }
                startConnectStep()
            }


            widgetList.root.setOnClickListener {
                if (!vm.canClick) {
                    Toast.makeText(
                        requireActivity(),
                        "Please try again later.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@setOnClickListener
                }
                if (!isLoadingSever && "seversSecretString".getKv().isEmpty()) {
                    loadSevers()
                    disLoading()
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(2000)
                        withContext(Dispatchers.Main) {
                            dismissLoading()
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    GreenActivity::class.java
                                )
                            )
                        }
                    }
                    return@setOnClickListener
                }
                startActivity(
                    Intent(
                        requireActivity(),
                        GreenActivity::class.java
                    )
                )
            }
        }

        changeState(BaseService.State.Idle)
        connection.connect(requireActivity(), this)
        DataStore.publicStore.registerChangeListener(this)

        if (!appSeverLists?.data?.smartList.isNullOrEmpty()) {
            changeSever(appSeverLists?.data?.smartList?.random())
        }
    }

//    private fun setNativeAdUI(it: Any?) {
//        if (it == null) {
//            binding.homeNative.root.visibility = View.INVISIBLE
//            return
//        }
//        mobNativeA.updateAdState(MobState.Showing)
//        binding.homeNative.root.visibility = View.VISIBLE
//        val ad = it as NativeAd
//        "nativeA ${ad.headline}  ${ad.body}".lightVDebugLog()
//        with(binding.homeNative) {
//            image.setImageDrawable(ad.icon?.drawable)
//            textAdTitle.text = ad.headline
//            textAdContent.text = ad.body
//            textInstallClick.text = ad.callToAction
//            nativeViewSmall.iconView = image
//            nativeViewSmall.headlineView = textAdTitle
//            nativeViewSmall.bodyView = textAdContent
//            nativeViewSmall.callToActionView = root
//            nativeViewSmall.setNativeAd(ad)
//        }
//        RedMob.resetLoadTime(mobNativeA)
//        RedMob.loadAd(context = requireActivity(), mobNativeA)
//    }


    override fun onResume() {
        super.onResume()
        globalCanUpdateHomeNative = false
        showHomeAd()
        with(binding) {
            if (currentSelectSever == null || vcurrentSelectSeverIsSmart) {
                textLocation.text = "ServerLocation: Smart"
                widgetList.severLogo.setImageResource(R.mipmap.smart_sever_logo)
                widgetList.severTitle.text = "Fastest Server"
            } else {
                textLocation.text = "ServerLocation: ${currentSelectSever!!.countryName}"
                widgetList.severLogo.setImageResource(getIntFromString(currentSelectSever!!.countryCode))
                widgetList.severTitle.text =
                    "${currentSelectSever!!.countryName} ${currentSelectSever!!.cityName}"
            }
        }

        when (resultGlobalAction) {
            "Fast Node" -> {
                changeSever(appSeverLists?.data?.smartList?.random(), true)
                startConnectStep()
            }

            else -> {}
        }
        resultGlobalAction = ""
    }

    private var switchStateWithoutRoute = false

    private fun startConnectStep() {
        if (isLoadingSever && "seversSecretString".getKv().isEmpty()) {
            disLoading()
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                withContext(Dispatchers.Main) {
                    dismissLoading()
                }
            }
            return
        }

        if (!isLoadingSever && "seversSecretString".getKv().isEmpty()) {
            loadSevers()
            disLoading()
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                withContext(Dispatchers.Main) {
                    dismissLoading()
                }
            }
            return
        }
        if (!isNetworkAvailable(requireActivity())) {
            YellowDialog().show(requireActivity().supportFragmentManager, "YellowDialog")
            return
        } else if (addressLimit) {
            OrangeDialog().show(requireActivity().supportFragmentManager, "OrangeDialog")
            return
        }

        if ("noFirstConnect".getKv().isEmpty()) {
            noAllowLaunchAgain = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher?.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                if (currentSelectSever == null) {
                    changeSever(appSeverLists?.data?.smartList?.random(), true)
                }
                switchVpnStateF()
            }
            return
        }

        noMoreClick(canClick = false, goalIsConnect)
        startProgressAnimation(goalIsConnect, ({
            noMoreClick(canClick = true, goalIsConnect)
        }))
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                switchVpnStateF()
                goalIsConnect = !goalIsConnect
            } else {
                noMoreClick(canClick = true, !goalIsConnect)
                resetUiState(!goalIsConnect)
            }
        }
    }

    var goalIsConnect = true

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
                binding.textTimer.setText(time)
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnable!!)
    }

    private fun resetUiState2(connected: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            progressAnimation?.cancel()
            goalIsConnect = !connected
            if (connected) {
                binding.textConnect.text = "Connected"
                binding.textConnectionState.text = "CONNECTED"
                binding.progressBar.progress = 100
                binding.progressBar.progressDrawable =
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.drawable_progress_end
                    )
                binding.backgroundImage.setBackgroundResource(R.mipmap.connected_background)
            } else {
                binding.textConnect.text = "Connect"
                binding.textConnectionState.text = "DISCONNECTED"
                binding.progressBar.progress = 100
                binding.progressBar.progressDrawable =
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.drawable_progress_start
                    )
                binding.backgroundImage.setBackgroundResource(R.mipmap.noconnect_background)
            }
        }
    }
    private fun resetUiState(connected: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            progressAnimation?.cancel()
            goalIsConnect = !connected
            if (isAdded) {
                if (connected) {
                    binding.textConnect.text = "Connected"
                    binding.textConnectionState.text = "CONNECTED"
                    binding.progressBar.progress = 100
                    binding.progressBar.progressDrawable =
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.drawable_progress_end
                        )
                    binding.backgroundImage.setBackgroundResource(R.mipmap.connected_background)
                } else {
                    binding.textConnect.text = "Connect"
                    binding.textConnectionState.text = "DISCONNECTED"
                    binding.progressBar.progress = 100
                    binding.progressBar.progressDrawable =
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.drawable_progress_start
                        )
                    binding.backgroundImage.setBackgroundResource(R.mipmap.noconnect_background)
                }
            } else {
                Log.w("RedFragment", "Fragment is not attached to an Activity, skipping UI update.")
            }
        }
    }


    var progressAnimation: ValueAnimator? = null
    private fun startProgressAnimation(isConnect: Boolean = true, task: (() -> Unit)? = null) {
        if (isConnect) {
            binding.textConnect.text = "Connecting..."
            binding.textConnectionState.text = "CONNECTING"
            binding.progressBar.progressDrawable =
                ContextCompat.getDrawable(requireActivity(), R.drawable.drawable_progress_ing)
            binding.backgroundImage.setBackgroundResource(R.mipmap.connecting_background)

            progressAnimation = ValueAnimator.ofInt(0, 100).apply {
                setDuration(10000)
                addUpdateListener {
                    binding.progressBar.progress = it.getAnimatedValue().toString().toInt()
                    if (binding.progressBar.progress == 100) {
                        binding.textConnect.text = "Connected"
                        binding.textConnectionState.text = "CONNECTED"
                        binding.progressBar.progressDrawable =
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.drawable_progress_end
                            )
                        binding.backgroundImage.setBackgroundResource(R.mipmap.connected_background)
                        task?.invoke()
                    }
                }
            }
            progressAnimation?.start()
        } else {
            binding.backgroundImage.setBackgroundResource(R.mipmap.connecting_background)
            binding.textConnect.text = "Disconnecting..."
            binding.textConnectionState.text = "DISCONNECTING"
            binding.progressBar.progressDrawable =
                ContextCompat.getDrawable(requireActivity(), R.drawable.drawable_progress_ing)

            progressAnimation = ValueAnimator.ofInt(0, 100).apply {
                setDuration(10000)
                addUpdateListener {
                    binding.progressBar.progress = it.getAnimatedValue().toString().toInt()
                    if (binding.progressBar.progress == 100) {
                        binding.textConnect.text = "Connect"
                        binding.textConnectionState.text = "DISCONNECTED"
                        binding.progressBar.progress = 100
                        binding.progressBar.progressDrawable =
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.drawable_progress_start
                            )
                        binding.backgroundImage.setBackgroundResource(R.mipmap.noconnect_background)
                        task?.invoke()
                    }
                }
            }
            progressAnimation?.start()
        }
    }


    private fun disLoading(show: Boolean = false) {
        RedDialog(show = show).show(requireActivity().supportFragmentManager, "RedDialog")
    }

    fun dismissLoading() {
        (requireActivity().supportFragmentManager.findFragmentByTag("RedDialog") as? DialogFragment)?.dismiss()
    }


    override fun trafficPersisted(profileId: Long) {
    }

    override fun onBinderDied() {
        connection.disconnect(requireActivity())
        connection.connect(requireActivity(), this)
    }

    override fun onStart() {
        super.onStart()
        connection.bandwidthTimeout = 500
    }

    override fun onStop() {
        connection.bandwidthTimeout = 0
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        DataStore.publicStore.unregisterChangeListener(this)
        connection.disconnect(requireActivity())
    }

    var onceInGood = false
    private fun onceIn() {
        if (!isAdded) return
        if (onceInGood) return
        onceInGood = true
        log("跳转---1")
        startActivity(
            Intent(
                requireActivity(),
                YellowActivity::class.java
            ).apply {
                putExtra("state", "connected")
                putExtra("second", seconds)
            })
        resetUiState(true)
        noMoreClick(canClick = true, goalIsConnect)
        startCount()
    }

    var onceOutGood = false
    @Synchronized
    private fun onceOut() {
        if (!isAdded) return
        if (onceOutGood) return
        onceOutGood = true
        log("跳转---2")
        startActivity(Intent(requireActivity(), YellowActivity::class.java).apply {
            putExtra("state", "disconnected")
            putExtra("second", seconds)
        })
        resetUiState(false)
        noMoreClick(canClick = true, !goalIsConnect)
        stopCount()
    }

    private fun showConnectAd(isConnect: Boolean, nextFun: () -> Unit) {
        var eed = 0
        val num = getLogicJson().eed ?: ""
        val (firstNumber, secondNumber) = AdUtils.splitStringToNumbers(num)
        // 使用 first 和 second
        eed = if (isConnect) {
            firstNumber
        } else {
            secondNumber
        }
        showConnectJob = lifecycleScope.launch() {
            val baseAd = BaseAd.getConnectInstance()
            val adConnectData = baseAd.appAdDataForest
            if (baseAd.canShowAd(requireActivity(), baseAd) == 0) {
                nextFun()
                return@launch
            }
            if (adConnectData == null) {
                baseAd.advertisementLoadingForest(requireActivity())
            }
            try {
                withTimeout(eed * 1000L) {
                    while (isActive) {
                        if (baseAd.canShowAd(requireActivity(), baseAd) == 2) {
                            baseAd.playIntAdvertisementForest2(
                                requireActivity(),
                                baseAd,
                                closeWindowFun = {
                                    showConnectJob?.cancel()
                                    showConnectJob = null
                                    nextFun()
                                    if (isConnect) {
                                        baseAd.advertisementLoadingForest(requireActivity())
                                    }
                                })
                            showConnectJob?.cancel()
                            showConnectJob = null
                            break
                        }
                        delay(500)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                showConnectJob?.cancel()
                showConnectJob = null
                nextFun()
            }
        }
    }

    private fun showHomeAd() {
        showHomeJob?.cancel()
        showHomeJob = null
        showHomeJob = lifecycleScope.launch {
            delay(300)
        if(lifecycle.currentState.name !=Lifecycle.State.RESUMED.name){
            return@launch
        }
        val baseAd = BaseAd.getHomeInstance()
        if (AdUtils.blockAdBlacklist()) {
            binding.adLayout.isVisible = false
            return@launch
        }
        if (!AdUtils.isVPNConnected()) {
            binding.adLayoutAdmob.isVisible = false
            binding.imgOcAd.isVisible = true
            return@launch
        }
        binding.adLayout.isVisible = true
        binding.imgOcAd.isVisible = true
        baseAd.advertisementLoadingForest(requireActivity())
        if (baseAd.canShowAd(requireActivity(), baseAd) == 0) {
            showHomeJob?.cancel()
            showHomeJob = null
            return@launch
        }
            while (isActive) {
                delay(500L)
                if (baseAd.canShowAd(requireActivity(), baseAd) == 2) {
                    baseAd.playNativeAdvertisementForest(this@RedFragment, baseAd)
                    showHomeJob?.cancel()
                    showHomeJob = null
                    break
                }
            }
        }
    }

    private fun loadAdMain() {
        BaseAd.getConnectInstance().advertisementLoadingForest(requireActivity())
        BaseAd.getBackEndInstance().advertisementLoadingForest(requireActivity())
        BaseAd.getHomeInstance().advertisementLoadingForest(requireActivity())
        BaseAd.getEndInstance().advertisementLoadingForest(requireActivity())
    }

    var isConnecting = false
    var isDisConnecting = false
    private fun changeState(state: BaseService.State) {
        ("changeState===>${state.name}  ${state.canStop}").lightVDebugLog()
        globalConnectState = state
        when (state) {
            BaseService.State.Idle -> {
            }

            BaseService.State.Connecting -> {
                isConnecting = true
            }

            BaseService.State.Connected -> {
                log("showConnectAd--loading-Connected--")

                if ("noFirstConnect".getKv().isEmpty()) {
                    if (globalConnectState == BaseService.State.Connected) {
                        log("showConnectAd--loading---noFirstConnect")
                        loadAdMain()
                    }
                    noMoreClick(canClick = false, goalIsConnect)
                    startProgressAnimation(goalIsConnect, ({
                        noMoreClick(canClick = true, goalIsConnect)
                    }))
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(2000L)
                        withContext(Dispatchers.Main) {
                            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                                binding.textTimer.visibility = View.VISIBLE
                                binding.widgetSpeed.root.visibility = View.VISIBLE
                                if (isConnecting) {
                                    isConnecting = false
                                    onceInGood = false
                                    showConnectAd(true) {
                                        onceIn()
                                    }
                                }
                                goalIsConnect = !goalIsConnect
                            } else {
                                resetUiState(!goalIsConnect)
                                noMoreClick(canClick = true, !goalIsConnect)
                            }
                        }
                    }
                    "No".putKv("noFirstConnect")
                } else {
                    loadAdMain()
                    binding.textTimer.visibility = View.VISIBLE
                    binding.widgetSpeed.root.visibility = View.VISIBLE
                    if (isConnecting) {
                        isConnecting = false
                        onceInGood = false
                        showConnectAd(true) {
                            onceIn()
                        }
                    }
                }
            }

            BaseService.State.Stopping -> {
                if (!isConnecting) {
                    isDisConnecting = true
                }
            }

            BaseService.State.Stopped -> {
                log("changeState----isConnecting=${isConnecting}---isDisConnecting=${isDisConnecting}")
                binding.textTimer.visibility = View.INVISIBLE
                binding.widgetSpeed.root.visibility = View.INVISIBLE
                if (isConnecting) {
                    resetUiState(false)
                    Toast.makeText(requireActivity(), "Please try again", Toast.LENGTH_LONG).show()
                    isConnecting = false
                }
                if (isDisConnecting) {
                    isDisConnecting = false
                    if (switchStateWithoutRoute) {
                        switchStateWithoutRoute = false
                        stopCount()
                    } else {
                        onceOutGood = false
                        onceOut()
                    }
                }
            }
        }
    }
    override fun onServiceConnected(service: IShadowsocksService) {
        log("showConnectAd--loading-1--${BaseService.State.values()[service.state].name}")
        changeState(
            try {
                BaseService.State.values()[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )
        resetUiState(AdUtils.isVPNConnected())
    }
    override fun onServiceDisconnected() {
        log("showConnectAd--loading-0--")

        changeState(BaseService.State.Idle)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        log("showConnectAd--loading-2--${state.name}")
        changeState(state)
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(requireActivity())
                connection.connect(requireActivity(), this)
            }
        }
    }

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        binding.widgetSpeed.downloadData.text =
            String.format("%s", Formatter.formatFileSize(requireActivity(), stats.rxRate))
        binding.widgetSpeed.uploadData.text =
            String.format("%s", Formatter.formatFileSize(requireActivity(), stats.txRate))
    }
}

var globalSpecialWaitTimeA = 0L
var globalSpecialLaunchAgainA = false

fun specialLaunchAgainA(activity: Activity) {
    if (globalSpecialLaunchAgainA) {
        globalSpecialLaunchAgainA = false
        globalSpecialWaitTimeA = 0L

        "special route A ".lightVDebugLog()
        val intent = Intent(activity, RedActivity::class.java)
        activity.startActivity(intent)
    }
}


var globalSpecialWaitTimeB = 0L
var globalSpecialLaunchAgainB = false

fun specialLaunchAgainB(activity: Activity) {
    if (globalSpecialLaunchAgainB) {
        globalSpecialLaunchAgainB = false
        globalSpecialWaitTimeB = 0L
        "special route B ".lightVDebugLog()
        val intent = Intent(activity, RedActivity::class.java)
        activity.startActivity(intent)
    }
}