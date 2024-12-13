package com.light.lightV.blue.ggggg

import android.content.Context
import android.graphics.Outline
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.light.lightV.R
import com.light.lightV.blue.ggggg.AdUtils.log
import com.light.lightV.green.getKv
import com.light.lightV.green.kv
import com.light.lightV.green.putKv
import com.light.lightV.orange.RedFragment
import com.light.lightV.red.YellowActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date


class BaseAd private constructor() {
    companion object {
        fun getOpenInstance() = BaseAdInstall.openLoadForest
        fun getHomeInstance() = BaseAdInstall.homeLoadForest
        fun getEndInstance() = BaseAdInstall.resultLoadForest
        fun getConnectInstance() = BaseAdInstall.connectLoadForest
        fun getBackEndInstance() = BaseAdInstall.backEndLoadForest
        fun getBackListInstance() = BaseAdInstall.backListLoadForest


        private var idCounter = 0
    }

    object BaseAdInstall {
        val openLoadForest = BaseAd()
        val homeLoadForest = BaseAd()
        val resultLoadForest = BaseAd()
        val connectLoadForest = BaseAd()
        val backEndLoadForest = BaseAd()
        val backListLoadForest = BaseAd()
    }

    private val id = getBaseId()
    var isFirstLoad: Boolean = false
    private var adDataOpen: ForestAdBean? = null
    private var adDataHome: ForestAdBean? = null
    private var adDataResult: ForestAdBean? = null
    private var adDataCont: ForestAdBean? = null
    private var adDataList: ForestAdBean? = null
    private var adDataEnd: ForestAdBean? = null
    private fun getBaseId(): Int {
        idCounter++
        return idCounter
    }

    var whetherToShowForest = false

    var loadTimeForest: Long = Date().time

    private val instanceName: String = getInstanceName()

    var appAdDataForest: Any? = null

    var isLoadingForest = false


    private fun adGuoQi(loadTime: Long): Boolean =
        Date().time - loadTime < 60 * 60 * 1000

    fun advertisementLoadingForest(context: Context) {
        if (isLoadingForest) {
            log("${getInstanceName()}-The ad is loading and cannot be loaded again")
            return
        }
        if (!AdUtils.isVPNConnected()) {
            log("${getInstanceName()}-The VPN is not connected, the ad cannot be loaded")
            return
        }
        if (limitIsExceeded()) {
            log("广告超限不在加载")
            return
        }
        val blacklistState = AdUtils.blockAdBlacklist()
        if (blacklistState && (instanceName == "connect" || instanceName == "backEnd" || instanceName == "backList" || instanceName == "home")) {
            log("黑名单屏蔽：${instanceName}广告，不加载")
            return
        }

        if (appAdDataForest == null) {
            isLoadingForest = true
            loadStartupPageAdvertisementForest(context, AdUtils.getAdJson())
        }
        if ((getLoadIp().isNotEmpty()) && getLoadIp() != AdUtils.connectIp && appAdDataForest != null) {
            Timber.e(getInstanceName() + "-ip不一致-重新加载-load_ip=" + getLoadIp() + "-now-ip=" + AdUtils.connectIp)
            whetherToShowForest = false
            appAdDataForest = null
            clearLoadIp()
            advertisementLoadingForest(context)
            return
        }
        if (appAdDataForest != null && !adGuoQi(loadTimeForest)) {
            isLoadingForest = true
            loadStartupPageAdvertisementForest(context, AdUtils.getAdJson())
        }
    }


    private fun loadStartupPageAdvertisementForest(context: Context, adData: ForestAdBean) {
        adLoaders[id]?.invoke(context, adData)
    }

    private val adLoaders = createAdLoadersMap()

    private fun createAdLoadersMap(): Map<Int, (Context, ForestAdBean) -> Unit> {
        val adLoadersMap = mutableMapOf<Int, (Context, ForestAdBean) -> Unit>()

        adLoadersMap[1] = { context, adData ->
            loadOpenAdForest(context, adData)
        }

        adLoadersMap[2] = { context, adData ->
            loadNativeAdvertisement(context, adData, getHomeInstance())
        }

        adLoadersMap[3] = { context, adData ->
            loadNativeAdvertisement(context, adData, getEndInstance())
        }

        adLoadersMap[4] = { context, adData ->
            loadIntAdvertisementForest(context, adData, getConnectInstance())
        }

        adLoadersMap[5] = { context, adData ->
            loadIntAdvertisementForest(context, adData, getBackEndInstance())

        }

        adLoadersMap[6] = { context, adData ->
            loadIntAdvertisementForest(context, adData, getBackListInstance())
        }

        return adLoadersMap
    }


    private fun loadOpenAdForest(context: Context, adData: ForestAdBean, currentIndex: Int = 0) {
        val adIds = AdUtils.getAdmobIdList(adData.rusty)
        if (currentIndex >= adIds.size) {
            if (!isFirstLoad) {
                isFirstLoad = true
                loadOpenAdForest(context, adData, 0)
                return
            }
            getOpenInstance().isLoadingForest = false
            log("${getInstanceName()}-所有广告 ID 加载失败")
            return
        }

        val currentAdId = adIds[currentIndex]
        log("${getInstanceName()}-开始加载广告 ID: $currentAdId")

        AdUtils.openTypeIp = AdUtils.connectIp
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            currentAdId,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    log("rusty ads start loading success")
                    getOpenInstance().isLoadingForest = false
                    getOpenInstance().appAdDataForest = ad
                    getOpenInstance().loadTimeForest = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    getOpenInstance().appAdDataForest = null

                    val error =
                        """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                    log("rusty ads start loading Failed=${error}")
                    loadOpenAdForest(context, adData, currentIndex + 1)
                }
            }
        )
    }


    private fun advertisingOpenCallbackForest(fullScreenFun: () -> Unit) {
        if (getOpenInstance().appAdDataForest !is AppOpenAd) {
            return
        }
        (getOpenInstance().appAdDataForest as AppOpenAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    getOpenInstance().whetherToShowForest = false
                    getOpenInstance().appAdDataForest = null
                    fullScreenFun()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    getOpenInstance().whetherToShowForest = false
                    getOpenInstance().appAdDataForest = null
                }

                override fun onAdShowedFullScreenContent() {
                    getOpenInstance().appAdDataForest = null
                    getOpenInstance().whetherToShowForest = true
                    AdUtils.setShowNum()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdUtils.setClickNum()
                }
            }
    }


    fun displayOpenAdvertisementForest(
        activity: AppCompatActivity,
        fullScreenFun: () -> Unit
    ): Boolean {
        if (getOpenInstance().appAdDataForest == null) {
            return false
        }
        if (getOpenInstance().whetherToShowForest || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return false
        }
        if ((AdUtils.openTypeIp.isNotEmpty()) && AdUtils.openTypeIp != AdUtils.connectIp) {
            log("rusty-ip不一致-不能展示-load_ip=" + AdUtils.openTypeIp + "-now-ip=" + AdUtils.connectIp)
            return false
        }
        log("rusty-ip一致-展示-load_ip=" + AdUtils.openTypeIp + "-now-ip=" + AdUtils.connectIp)
        advertisingOpenCallbackForest(fullScreenFun)
        (getOpenInstance().appAdDataForest as AppOpenAd).show(activity)
        clearLoadIp()
        return true
    }

    private fun loadNativeAdvertisement(
        context: Context,
        adData: ForestAdBean,
        adBase: BaseAd?,
        currentIndex: Int = 0
    ) {
        if (adBase == null) {
            log("adBase is null")
            return
        }

        // 获取广告 ID 列表
        val adIds = when (adBase) {
            getHomeInstance() -> AdUtils.getAdmobIdList(adData.usage)
            getEndInstance() -> AdUtils.getAdmobIdList(adData.round)
            else -> AdUtils.getAdmobIdList(adData.usage)
        }

        // 检查广告 ID 列表有效性
        if (adIds.isEmpty() || currentIndex >= adIds.size) {
            adBase.isLoadingForest = false
            log("${adBase.getInstanceName()}-所有广告 ID 加载失败")
            return
        }

        val currentAdId = adIds[currentIndex]
        log("${adBase.getInstanceName()}-开始加载广告 ID: $currentAdId")

        // 设置 IP 和广告数据
        when (adBase) {
            getHomeInstance() -> {
                AdUtils.homeTypeIp = AdUtils.connectIp
            }

            getEndInstance() -> {
                AdUtils.endTypeIp = AdUtils.connectIp
            }
        }

        try {
            // 构建广告加载器
            AdLoader.Builder(context.applicationContext, currentAdId)
                .apply {
                    withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
                            .build()
                    )

                    forNativeAd {
                        adBase.appAdDataForest = it
                        log("${adBase.getInstanceName()}-广告加载成功")
                        it.setOnPaidEventListener { adValue ->
                            getNatData(it, adValue, currentAdId, adBase)
                            if (adBase == getHomeInstance()) {
                                getHomeInstance().advertisementLoadingForest(context)
                            }
                        }
                    }

                    withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            log("${adBase.getInstanceName()}-广告加载失败: ${loadAdError.message}")
                            adBase.appAdDataForest = null
                            loadNativeAdvertisement(context, adData, adBase, currentIndex + 1)
                        }

                        override fun onAdLoaded() {
                            adBase.loadTimeForest = Date().time
                            adBase.isLoadingForest = false
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            log("点击原生广告")
                            AdUtils.setClickNum()
                        }
                    })
                }
                .build()
                .loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            log("Error building AdLoader or AdRequest: ${e.message}")
        }
    }

    private fun loadIntAdvertisementForest(
        context: Context,
        adData: ForestAdBean,
        adBase: BaseAd?,
        currentIndex: Int = 0
    ) {
        if (adBase == null) {
            log("adBase is null")
            return
        }

        // 获取广告 ID 列表
        val adIds = when (adBase) {
            getConnectInstance() -> AdUtils.getAdmobIdList(adData.yowza)
            getBackEndInstance() -> AdUtils.getAdmobIdList(adData.badly)
            else -> AdUtils.getAdmobIdList(adData.jens)
        }

        // 检查广告 ID 列表有效性
        if (adIds.isEmpty() || currentIndex >= adIds.size) {
            adBase.isLoadingForest = false
            log("${adBase.getInstanceName()}-所有广告 ID 加载失败")
            return
        }

        val currentAdId = adIds[currentIndex]
        log("${adBase.getInstanceName()}-开始加载广告 ID: $currentAdId")

        val adRequest = AdRequest.Builder().build()
        when (adBase) {
            getConnectInstance() -> {
                AdUtils.contTypeIp = AdUtils.connectIp
            }

            getBackEndInstance() -> {
                AdUtils.backEndTypeIp = AdUtils.connectIp
            }

            getBackListInstance() -> {
                AdUtils.backListTypeIp = AdUtils.connectIp
            }
        }
        InterstitialAd.load(
            context,
            currentAdId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adBase.appAdDataForest = null
                    val error =
                        """
           domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}
          """
                    log("${adBase.getInstanceName()}-The ad failed to load:$error ")
                    loadIntAdvertisementForest(context, adData, adBase, currentIndex + 1)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    log("${adBase.getInstanceName()}-The ad loads successfully: ")
                    adBase.loadTimeForest = Date().time
                    adBase.isLoadingForest = false
                    adBase.appAdDataForest = interstitialAd
                }
            })
    }


    private fun intScreenAdCallback(adBase: BaseAd, closeWindowFun: () -> Unit) {
        (adBase.appAdDataForest as? InterstitialAd)?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    AdUtils.setClickNum()
                }

                override fun onAdDismissedFullScreenContent() {
                    adBase.appAdDataForest = null
                    adBase.whetherToShowForest = false
                    closeWindowFun()

                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    adBase.appAdDataForest = null
                    adBase.whetherToShowForest = false
                }

                override fun onAdImpression() {
                }

                override fun onAdShowedFullScreenContent() {
                    adBase.appAdDataForest = null
                    adBase.whetherToShowForest = true
                    AdUtils.setShowNum()
                }
            }
    }

    fun canShowAd(
        activity: FragmentActivity,
        adBase: BaseAd
    ): Int {
        if (!AdUtils.isVPNConnected() || getOpenInstance().limitIsExceeded()) {
            return 0
        }
        val blacklistState = AdUtils.blockAdBlacklist()
        if (blacklistState && (adBase == getConnectInstance() || adBase == getBackEndInstance() || adBase == getBackListInstance() || adBase == getHomeInstance())) {
            log("黑名单屏蔽：${adBase.getInstanceName()}广告，不显示")
            return 0
        }
        if (adBase.appAdDataForest == null) {
            return 1
        }

        if (adBase.whetherToShowForest || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return 1
        }
        val vpnIp = AdUtils.connectIp
        if ((getLoadIp().isNotEmpty()) && getLoadIp() != vpnIp) {
            log("${adBase.getInstanceName()}-ip不一致-不能展示-load_ip=" + getLoadIp() + "-now-ip=" + vpnIp)
            return 0
        }
        return 2
    }

    fun playIntAdvertisementForest(
        activity: AppCompatActivity,
        adBase: BaseAd,
        closeWindowFun: () -> Unit
    ) {
        log("${adBase.getInstanceName()}-ip一致-展示-load_ip=" + getLoadIp() + "-now-ip=" + AdUtils.connectIp)
        intScreenAdCallback(adBase, closeWindowFun)
        activity.lifecycleScope.launch(Dispatchers.Main) {
            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                (adBase.appAdDataForest as InterstitialAd).show(activity)
                clearLoadIp()
            }
        }
    }

    fun playIntAdvertisementForest2(
        activity: FragmentActivity,
        adBase: BaseAd,
        closeWindowFun: () -> Unit
    ) {
        log("${adBase.getInstanceName()}-ip一致-展示-load_ip=" + getLoadIp() + "-now-ip=" + AdUtils.connectIp)
        intScreenAdCallback(adBase, closeWindowFun)
        activity.lifecycleScope.launch(Dispatchers.Main) {
            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                (adBase.appAdDataForest as InterstitialAd).show(activity)
                clearLoadIp()
            }
        }
    }

    fun playNativeAdvertisementForest(
        fragment: Fragment,
        adBase: BaseAd,
    ) {
        log("${adBase.getInstanceName()}-ip一致-展示-load_ip=" + getLoadIp() + "-now-ip=" + AdUtils.connectIp)
        setDisplayHomeNativeAdForest(fragment as RedFragment, adBase)
    }

    fun playNativeEnd(
        activity: YellowActivity,
        adBase: BaseAd,
    ) {
        log("${adBase.getInstanceName()}-ip一致-展示-load_ip=" + getLoadIp() + "-now-ip=" + AdUtils.connectIp)
        setDisplayEndNativeAdForest(activity, adBase)
    }


    private fun getNatData(ad: NativeAd, adValue: AdValue, loadId: String, adBase: BaseAd) {
        val bean = when (adBase) {
            getHomeInstance() -> {
                adDataHome
            }

            getEndInstance() -> {
                adDataResult
            }

            else -> {
                null
            }
        }
        val adWhere = when (adBase) {
            getHomeInstance() -> {
                "usage"
            }

            getEndInstance() -> {
                "round"
            }

            else -> {
                ""
            }
        }
    }

    private fun setDisplayHomeNativeAdForest(activity: RedFragment, adBase: BaseAd) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (adBase.appAdDataForest as NativeAd).let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED

                if (state) {
                    activity.binding.imgOcAd.isVisible = true
                    val adView = activity.layoutInflater.inflate(
                        R.layout.native_red,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.binding.adLayoutAdmob.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.binding.imgOcAd.isVisible = false
                    activity.binding.adLayoutAdmob.isVisible = true
                    adBase.appAdDataForest = null
                    adBase.isLoadingForest = false
                    AdUtils.homeTypeIp = ""
                    AdUtils.setShowNum()
                }
            }
        }
    }

    private fun setDisplayEndNativeAdForest(activity: YellowActivity, adBase: BaseAd) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (adBase.appAdDataForest as NativeAd).let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED
                if (state) {
                    activity.binding.imgOcAd.isVisible = true
                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(
                        R.layout.native_orange,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.binding.adLayoutAdmob.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.binding.imgOcAd.isVisible = false
                    activity.binding.adLayoutAdmob.isVisible = true
                    adBase.appAdDataForest = null
                    adBase.isLoadingForest = false
                    AdUtils.endTypeIp = ""
                }
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.text_ad_title)
        adView.bodyView = adView.findViewById(R.id.text_ad_content)
        adView.callToActionView = adView.findViewById(R.id.text_install_click)
        adView.iconView = adView.findViewById(R.id.image)
        adView.mediaView = adView.findViewById(R.id.image_content)

        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }?.mediaContent =
                it
        }
        adView.mediaView?.clipToOutline = true
        adView.mediaView?.outlineProvider = NavigationViewOutlineProvider()
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }
    class NavigationViewOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            val sView = view ?: return
            val sOutline = outline ?: return
            sOutline.setRoundRect(
                0,
                0,
                sView.width,
                sView.height,
                8f
            )
        }
    }
    private fun getLoadIp(): String {
        return when (getInstanceName()) {
            "rusty" -> AdUtils.openTypeIp
            "home" -> AdUtils.homeTypeIp
            "end" -> AdUtils.endTypeIp
            "connect" -> AdUtils.contTypeIp
            "backEnd" -> AdUtils.backEndTypeIp
            "backList" -> AdUtils.backListTypeIp
            else -> {
                ""
            }
        }
    }

    private fun clearLoadIp() {
        when (getInstanceName()) {
            "rusty" -> AdUtils.openTypeIp = ""
            "home" -> AdUtils.homeTypeIp = ""
            "end" -> AdUtils.endTypeIp = ""
            "connect" -> AdUtils.contTypeIp = ""
            "backEnd" -> AdUtils.backEndTypeIp = ""
            "backList" -> AdUtils.backListTypeIp = ""
            else -> {
                ""
            }
        }
    }

    private fun getInstanceName(): String {
        return when (id) {
            1 -> "rusty"
            2 -> "home"
            3 -> "end"
            4 -> "connect"
            5 -> "backEnd"
            6 -> "backList"
            else -> ""
        }
    }

    private fun getLoadIdLog(adBean: ForestAdBean): String {
        return when (id) {
            1 -> "rusty+${adBean.rusty}"
            2 -> "home+${adBean.usage}"
            3 -> "end+${adBean.round}"
            4 -> "connect+${adBean.yowza}"
            5 -> "backEnd+${adBean.badly}"
            6 -> "backList+${adBean.jens}"
            else -> ""
        }
    }

    private fun getLoadId(adBean: ForestAdBean): String {
        return when (id) {
            1 -> adBean.rusty
            2 -> adBean.usage
            3 -> adBean.round
            4 -> adBean.yowza
            5 -> adBean.badly
            6 -> adBean.jens
            else -> ""
        }
    }

    fun isAppOpenSameDayBa() {
        if (AdUtils.getAdLoadDateNum().isBlank()) {
            AdUtils.setAdLoadDateNum(formatDateNow())
        } else {
            if (dateAfterDate(AdUtils.getAdLoadDateNum(), formatDateNow())) {
                AdUtils.setAdLoadDateNum(formatDateNow())
                log("超限-清除数据:")
                kv.encode(AdUtils.local_c_n, 0)
                kv.encode(AdUtils.local_s_n, 0)
            }
        }
    }

    private fun formatDateNow(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return simpleDateFormat.format(date)
    }

    private fun dateAfterDate(startTime: String?, endTime: String?): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd")
        try {
            val startDate: Date = format.parse(startTime)
            val endDate: Date = format.parse(endTime)
            val start: Long = startDate.getTime()
            val end: Long = endDate.getTime()
            if (end > start) {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }


    fun limitIsExceeded(): Boolean {
        isAppOpenSameDayBa()
        val zongNum = AdUtils.getAdJson().actionttttttt
        val (firstNumber, secondNumber) = AdUtils.splitStringToNumbers(zongNum)
        val currentOpenCount = AdUtils.getShowNum() ?: 0
        val currentClickCount = AdUtils.getClickNum() ?: 0
        return (currentOpenCount >= firstNumber) || (currentClickCount >= secondNumber)
    }

}

