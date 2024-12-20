package com.light.lightV.blue.ggggg

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.webkit.WebSettings
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.light.lightV.blue.ggggg.AdUtils.postPutData
import com.light.lightV.green.RedApp
import com.light.lightV.green.getKv
import com.light.lightV.green.putKv
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

object TTUtils {
    private fun getLimitTracking(context: Context): String {
        return try {
            if (AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled) {
                "togo"
            } else {
                "saratoga"
            }
        } catch (e: Exception) {
            "saratoga"
        }
    }

    private fun getWebDefaultUserAgent(context: Context): String {
        return try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getLastUpdateTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.lastUpdateTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getPrecisionType(precisionType: Int): String {
        return when (precisionType) {
            0 -> {
                "UNKNOWN"
            }

            1 -> {
                "ESTIMATED"
            }

            2 -> {
                "PUBLISHER_PROVIDED"
            }

            3 -> {
                "PRECISE"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

    private fun getAppVersion(): String {
        try {
            val packageInfo = RedApp.redApp.packageManager.getPackageInfo(
                RedApp.redApp.packageName,
                0
            )

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Version information not available"
    }

    private fun firstJsonData(
        isAd: Boolean = false,
        adBean: ForestAdBean? = null
    ): JSONObject {
        val simmer = JSONObject().apply {
            //client_ts
            put("length", System.currentTimeMillis())
            //log_id
            put("aaas", UUID.randomUUID().toString())
            //distinct_id
            put("prior", AdUtils.andoridIdTba.getKv())
            //system_language
            put("devious", "${Locale.getDefault().language}_${Locale.getDefault().country}")
        }

        val fleming = JSONObject().apply {
            //manufacturer
            put("endogamy", "111")
            //bundle_id
            put("bathos", RedApp.redApp.packageName)
            //device_model
            put("beecham", "1")
            //operator
            put("goblet", "111")
            //android_id
            put("laurie", "1")
            //os_version
            put("rascal", "1")
            //os
            put("dynamic", "codify")
            //app_version
            put("taper", getAppVersion())
            //gaid
            put("routine", AdUtils.gIdTba.getKv())
        }

        return JSONObject().apply {
            put("simmer", simmer)
            put("fleming", fleming)
        }
    }

    private fun getSessionJson(): String {
        return firstJsonData().apply {
            put("knox", "sortie")
        }.toString()
    }

    private fun getInstallJson(referrerDetails: ReferrerDetails): String {
        return firstJsonData().apply {
            //build
            put("patsy", "build/${Build.ID}")

            //referrer_url
            put("shelter", referrerDetails.installReferrer)

            //install_version
            put("good", referrerDetails.installVersion)

            //user_agent
            put("harvard", getWebDefaultUserAgent(RedApp.redApp))

            //lat
            put("spatial", getLimitTracking(RedApp.redApp))

            //referrer_click_timestamp_seconds
            put("diamond", referrerDetails.referrerClickTimestampSeconds)

            //install_begin_timestamp_seconds
            put("fiscal", referrerDetails.installBeginTimestampSeconds)

            //referrer_click_timestamp_server_seconds
            put("headmen", referrerDetails.referrerClickTimestampServerSeconds)

            //install_begin_timestamp_server_seconds
            put("tribal", referrerDetails.installBeginTimestampServerSeconds)

            //install_first_seconds
            put("tinfoil", getFirstInstallTime(RedApp.redApp))

            //last_update_seconds
            put("bole", getLastUpdateTime(RedApp.redApp))

        }.toString()
    }

    fun emitSessionData() {
        val json = getSessionJson()
        AdUtils.log("json-getSessionJson--->${json}")
        try {
            postPutData(
                json,
                object : AdUtils.CallbackPost {
                    override fun onSuccess(response: String) {
                        AdUtils.log("Session事件上报-成功->")
                    }

                    override fun onFailure(error: String) {
                        AdUtils.log("Session事件上报-失败=$error")

                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "Session事件上报-失败=$e")

        }
    }

    fun emitInstallData(context: Context, referrerDetails: ReferrerDetails) {
        val state = AdUtils.installTbaState.getKv()
        if (state == "1") {
            return
        }
        val json = getInstallJson(referrerDetails)
        Log.e("TBA", "json-install--->${json}")
        try {
            postPutData(
                json,
                object : AdUtils.CallbackPost {
                    override fun onSuccess(response: String) {
                        Log.e("TAG", "install事件上报-成功->")
                        "1".putKv(AdUtils.installTbaState)
                    }

                    override fun onFailure(error: String) {
                        Log.e("TAG", "install事件上报-失败=$error")
                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "install事件上报-失败=$e")

        }
    }
}