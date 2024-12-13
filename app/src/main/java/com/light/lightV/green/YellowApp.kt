package com.light.lightV.green

import com.light.lightV.blue.MobName
import com.light.lightV.blue.MobState
import com.light.lightV.blue.MobType
import com.light.lightV.blue.OrangeMob
import com.light.lightV.blue.YellowMob
import com.tencent.mmkv.MMKV
import org.json.JSONObject


lateinit var mobOpenId: YellowMob
lateinit var mobNativeAId: YellowMob
lateinit var mobNativeBId: YellowMob
lateinit var mobInterstitialAId: YellowMob
lateinit var mobInterstitialBId: YellowMob
lateinit var mobInterstitialCId: YellowMob

lateinit var mobOpen: OrangeMob
lateinit var mobNativeA: OrangeMob
lateinit var mobNativeB: OrangeMob
lateinit var mobInterstitialA: OrangeMob
lateinit var mobInterstitialB: OrangeMob
lateinit var mobInterstitialC: OrangeMob

var topClick = 3
var topShow = 30
var isLimit = false

val kv: MMKV by lazy { MMKV.mmkvWithID("GlobalMMKV", MMKV.MULTI_PROCESS_MODE) }

fun initRed(content: String) {
    if (content.isEmpty()) return
    runCatching {
        JSONObject(content).apply {
            val top = optString("actionttttttt").split("spacer")
            val rusty: List<String> = if (optString("rusty").contains("&")) {
                optString("rusty").split("&")
            } else {
                arrayListOf(optString("rusty"))
            }
            val nativeA: List<String> = if (optString("usage").contains("&")) {
                optString("usage").split("&")
            } else {
                arrayListOf(optString("usage"))
            }
            val nativeB: List<String> = if (optString("round").contains("&")) {
                optString("round").split("&")
            } else {
                arrayListOf(optString("round"))
            }
            val inA: List<String> = if (optString("yowza").contains("&")) {
                optString("yowza").split("&")
            } else {
                arrayListOf(optString("yowza"))
            }
            val inB: List<String> = if (optString("badly").contains("&")) {
                optString("badly").split("&")
            } else {
                arrayListOf(optString("badly"))
            }
            val inC: List<String> = if (optString("jens").contains("&")) {
                optString("jens").split("&")
            } else {
                arrayListOf(optString("jens"))
            }

            topClick = top[0].toInt()
            topShow = top[1].toInt()

            mobOpenId = YellowMob(
                idIndex = 0,
                idList = rusty as ArrayList<String>
            )
            mobNativeAId = YellowMob(
                idIndex = 0,
                idList = nativeA as ArrayList<String>
            )
            mobNativeBId = YellowMob(
                idIndex = 0,
                idList = nativeB as ArrayList<String>
            )
            mobInterstitialAId = YellowMob(
                idIndex = 0,
                idList = inA as ArrayList<String>
            )
            mobInterstitialBId = YellowMob(
                idIndex = 0,
                idList = inB as ArrayList<String>
            )
            mobInterstitialCId = YellowMob(
                idIndex = 0,
                idList = inC as ArrayList<String>
            )
        }
    }
}

fun initOrange() {
    mobOpen = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.LaunchOpen,
        mobType = MobType.Open,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobOpenId.getTopLoadTime(true)
    )
    mobNativeA = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.NativeHomeTop,
        mobType = MobType.Native,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobNativeAId.getTopLoadTime()
    )
    mobNativeB = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.NativeResultBottom,
        mobType = MobType.Native,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobNativeBId.getTopLoadTime()
    )
    mobInterstitialA = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.InterstitialConnect,
        mobType = MobType.Interstitial,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobInterstitialAId.getTopLoadTime()
    )
    mobInterstitialB = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.InterstitialServerBack,
        mobType = MobType.Interstitial,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobInterstitialBId.getTopLoadTime()
    )
    mobInterstitialC = OrangeMob(
        mobState = MobState.Empty,
        mobName = MobName.InterstitialResultBack,
        mobType = MobType.Interstitial,
        mobAd = null,
        timePoint = null,
        stateAction = null,
        isBlock = false,
        reLoadTime = 0,
        topLoadTime = mobInterstitialCId.getTopLoadTime()
    )
}


var adminConfig = "1"
var noVpnConfig = "1"
fun initYellow(content: String) {
    if (content.isEmpty()) return
    runCatching {
        JSONObject(content).apply {
            adminConfig = optString("ouch")
        }
    }
}

fun adminIsDanger(): Boolean {
    val admin = "adminType".getKv()
    return if (admin.isNotEmpty()) {
        admin == "lubbock"
    } else {
        false
    }
}

fun updateBlockState() {
    val block = adminConfig == "1" && adminIsDanger()
    mobInterstitialB.isBlock = block
    mobInterstitialA.isBlock = block
    mobInterstitialC.isBlock = block
    mobNativeA.isBlock = block
    "isblock  ===> ${block}".lightVDebugLog()
}