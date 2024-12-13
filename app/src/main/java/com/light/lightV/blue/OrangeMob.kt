package com.light.lightV.blue

import com.light.lightV.green.getKv
import com.light.lightV.green.isLimit
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.mobInterstitialAId
import com.light.lightV.green.mobInterstitialBId
import com.light.lightV.green.mobInterstitialCId
import com.light.lightV.green.mobNativeAId
import com.light.lightV.green.mobNativeBId
import com.light.lightV.green.mobOpenId
import com.light.lightV.green.putKv
import com.light.lightV.green.toDayLimitKey
import com.light.lightV.green.topClick
import com.light.lightV.green.topShow


class OrangeMob(
    var mobState: MobState,
    var mobName: MobName,
    var mobType: MobType,
    var mobAd: Any? = null,
    var timePoint: Long? = null,
    var stateAction: (() -> Unit)? = null,
    var isBlock: Boolean = false,
    var reLoadTime: Int = 0,
    var topLoadTime: Int = 0,
) {
    fun getId(): String {
        return when (mobName) {
            MobName.LaunchOpen -> mobOpenId.getId()
            MobName.NativeHomeTop -> mobNativeAId.getId()
            MobName.NativeResultBottom -> mobNativeBId.getId()
            MobName.InterstitialConnect -> mobInterstitialAId.getId()
            MobName.InterstitialServerBack -> mobInterstitialBId.getId()
            MobName.InterstitialResultBack -> mobInterstitialCId.getId()
        }
    }

    fun reset() {
        when (mobName) {
            MobName.LaunchOpen -> mobOpenId.reset()
            MobName.NativeHomeTop -> mobNativeAId.reset()
            MobName.NativeResultBottom -> mobNativeBId.reset()
            MobName.InterstitialConnect -> mobInterstitialAId.reset()
            MobName.InterstitialServerBack -> mobInterstitialBId.reset()
            MobName.InterstitialResultBack -> mobInterstitialCId.reset()
        }
    }

    fun next() {
        when (mobName) {
            MobName.LaunchOpen -> mobOpenId.next()
            MobName.NativeHomeTop -> mobNativeAId.next()
            MobName.NativeResultBottom -> mobNativeBId.next()
            MobName.InterstitialConnect -> mobInterstitialAId.next()
            MobName.InterstitialServerBack -> mobInterstitialBId.next()
            MobName.InterstitialResultBack -> mobInterstitialCId.next()
        }
    }

    fun updateExpire(resetAd: Boolean = true): Boolean {
        "updateExpire  ${mobAd != null}  ${mobState.name}  ${timePoint} ${System.currentTimeMillis() - (timePoint ?: 0)} ${mobType.name}".lightVDebugLog()
        if (mobAd != null && mobState == MobState.GetSuccess && timePoint != null) {
            if (mobType == MobType.Open && System.currentTimeMillis() - timePoint!! > 240 * 60 * 1000) {
                if (resetAd) {
                    updateAdState(MobState.Empty)
                }
                return true
            } else if (mobType == MobType.Interstitial && System.currentTimeMillis() - timePoint!! > 50 * 60 * 1000) {
                if (resetAd) {
                    updateAdState(MobState.Empty)
                }
                return true
            } else if (mobType == MobType.Native && System.currentTimeMillis() - timePoint!! > 50 * 60 * 1000) {
                if (resetAd) {
                    updateAdState(MobState.Empty)
                }
                return true
            }
        }
        return false
    }

    fun canShowAd(): Boolean {
        return !(isLimit || isBlock || mobState != MobState.GetSuccess)
    }

    fun canLoadAd(): Boolean {
        "can loadAd ${isLimit}  ${isBlock}  ${mobState.name}  ${reLoadTime}  ${topLoadTime}".lightVDebugLog()
        return !(isLimit
                || isBlock
                || mobState == MobState.Getting
                || mobState == MobState.GetSuccess
                || reLoadTime > topLoadTime)
    }

    fun updateAdState(state: MobState) {
        mobState.name.lightVDebugLog(
            before = "${mobName.name}  before state ",
            after = "  " + getId()
        )
        mobState = state
        when (mobState) {
            MobState.Empty -> {
                reset()
                mobAd = null
                mobState = MobState.Empty
            }

            MobState.Getting -> {
                reLoadTime += 1
            }

            MobState.GetFail -> {
                next()
            }

            MobState.GetSuccess -> {
                timePoint = System.currentTimeMillis()
                reLoadTime = 0
            }

            MobState.Showing -> {
                addShow()
            }

            MobState.ShowFail -> {
                reset()
                stateAction?.invoke()
            }

            MobState.ShowFinish -> {
                reset()
                stateAction?.invoke()
            }

            MobState.ClickShowing -> {
                addClick()
            }
        }
        mobState.name.lightVDebugLog(
            before = "${mobName.name}  after state ",
            after = "  " + getId()
        )
    }
}

fun addClick() {
    val name = ("1".toDayLimitKey() + "Click")
    (1.getKv(name) + 1).putKv(name)
}

fun addShow() {
    val name = ("1".toDayLimitKey() + "Show")
    (1.getKv(name) + 1).putKv(name)
}

fun updateLimit() {
    val clickTime = 1.getKv(("1".toDayLimitKey() + "Click"))
    val showTime = 1.getKv(("1".toDayLimitKey() + "Show"))
    isLimit = clickTime >= topClick || showTime >= topShow
    "click time ${clickTime}    show time ${showTime}   top click $topClick   top show $topShow   isLimit ${isLimit}".lightVDebugLog()
}