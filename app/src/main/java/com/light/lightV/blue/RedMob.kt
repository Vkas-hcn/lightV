package com.light.lightV.blue

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.updateBlockState

object RedMob {
    private fun getShowCall(orangeMob: OrangeMob): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdClicked() {
                orangeMob.updateAdState(MobState.ClickShowing)
            }

            override fun onAdDismissedFullScreenContent() {
                orangeMob.updateAdState(MobState.ShowFinish)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                p0.message.lightVDebugLog()
                orangeMob.updateAdState(MobState.ShowFail)
            }

            override fun onAdImpression() {
            }

            override fun onAdShowedFullScreenContent() {
                orangeMob.updateAdState(MobState.Showing)
            }
        }
    }

    private fun getLoadCall(context: Context, orangeMob: OrangeMob): Any {
        if (orangeMob.mobType == MobType.Open) {
            return object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    orangeMob.updateAdState(MobState.GetSuccess)
                    orangeMob.mobAd = p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    orangeMob.updateAdState(MobState.GetFail)
                    loadAd(context, orangeMob)
                }
            }

        } else {
            return object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    orangeMob.updateAdState(MobState.GetSuccess)
                    orangeMob.mobAd = p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    orangeMob.updateAdState(MobState.GetFail)
                    loadAd(context, orangeMob)
                }
            }
        }
    }

    private lateinit var nativeAdLoader: AdLoader


    fun resetLoadTime(orangeMob: OrangeMob) {
        if (orangeMob.mobState != MobState.Getting) {
            orangeMob.reLoadTime = 0
        }
    }

    fun loadAd(context: Context, orangeMob: OrangeMob) {
        updateLimit()
        updateBlockState()
        orangeMob.updateExpire(true)
        if (!orangeMob.canLoadAd()) {
            return
        }
        val request = AdRequest.Builder().build()
        orangeMob.updateAdState(MobState.Getting)
        if (orangeMob.mobType == MobType.Open) {
            AppOpenAd.load(
                context,
                orangeMob.getId(),
                request,
                getLoadCall(context, orangeMob) as AppOpenAdLoadCallback
            )
        } else if (orangeMob.mobType == MobType.Interstitial) {
            InterstitialAd.load(
                context,
                orangeMob.getId(),
                request,
                getLoadCall(context, orangeMob) as InterstitialAdLoadCallback
            )
        } else {
            nativeAdLoader = AdLoader.Builder(context, orangeMob.getId())
                .withAdListener(object : AdListener() {
                    override fun onAdClicked() {
                        "nativeClick".lightVDebugLog()
                        orangeMob.updateAdState(MobState.ClickShowing)
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        orangeMob.updateAdState(MobState.GetFail)
                        loadAd(context, orangeMob)
                    }
                })
                .forNativeAd {
                    if (nativeAdLoader.isLoading) {
                        orangeMob.updateAdState(MobState.Getting)
                    } else {
                        orangeMob.updateAdState(MobState.GetSuccess)
                        orangeMob.mobAd = it
                    }
                }.build()
            nativeAdLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun showAd(orangeMob: OrangeMob, activity: Activity) {
        updateLimit()
        updateBlockState()
        if (!orangeMob.canShowAd() || orangeMob.updateExpire(false)) {
            return
        }

        if (orangeMob.mobState == MobState.GetSuccess) {
            if (orangeMob.mobAd is AppOpenAd) {
                (orangeMob.mobAd as AppOpenAd).fullScreenContentCallback = getShowCall(orangeMob)
                (orangeMob.mobAd as AppOpenAd).show(activity)
            } else {
                (orangeMob.mobAd as InterstitialAd).fullScreenContentCallback =
                    getShowCall(orangeMob)
                (orangeMob.mobAd as InterstitialAd).show(activity)
            }
        }
    }
}

