package com.light.lightV.blue.ggggg

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.github.shadowsocks.bg.BaseService
import com.google.gson.Gson
import com.light.lightV.BuildConfig
import com.light.lightV.green.RedApp
import com.light.lightV.green.getKv
import com.light.lightV.green.getRealLimitConfig
import com.light.lightV.green.kv
import com.light.lightV.green.putKv
import com.light.lightV.green.redDebugConfig
import com.light.lightV.green.redReleaseConfig
import com.light.lightV.green.yellowReleaseConfig
import com.light.lightV.orange.globalConnectState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object AdUtils {
    var connectIp = ""
    var connectCity = ""
    var openTypeIp = ""
    var homeTypeIp = ""
    var endTypeIp = ""
    var contTypeIp = ""
    var backEndTypeIp = ""
    var backListTypeIp = ""

    const val blockData = "blockData"
    const val local_s_n = "local_s_n"
    const val local_c_n = "local_c_n"
    const val ad_load_date = "ad_load_date"
    const val cmpState = "cmpState"
    fun log(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e("TAG", msg)
        }
    }
    fun splitStringToNumbers(input: String): Pair<Int, Int> {
        val regex = Regex("\\d+")
        val matches = regex.findAll(input).map { it.value.toInt() }.toList()

        return if (matches.size == 2) {
            Pair(matches[0], matches[1])
        } else {
            Pair(0, 0)
        }
    }
    fun isVPNConnected(): Boolean {
        return globalConnectState == BaseService.State.Connected
    }

    fun blockAdBlacklist(): Boolean {
        val logicJson = getLogicJson() ?: return true
        val ouch = logicJson.ouch ?: return true

        val res = when (ouch) {
            "1" -> blockData.getKv() != "coinage"
            "2" -> false
            else -> true
        }
        return res
    }

    fun getLogicJson(): ForestLogicBean {
        val dataJson =
            "often".getKv().ifEmpty {
               getRealLimitConfig()
            }
        return runCatching {
            fromLogicJson(dataJson)
        }.getOrNull() ?: fromLogicJson(getRealLimitConfig())
    }

    fun getAdJson(): ForestAdBean {
        val dataJson =
            "under".getKv().ifEmpty {
                getIsRelease()
            }
        return runCatching {
            fromAdJson(dataJson)
        }.getOrNull() ?: fromAdJson(getIsRelease())
    }

    private fun getIsRelease(): String {
        return if (BuildConfig.DEBUG) {
            redDebugConfig
        } else {
            redReleaseConfig
        }
    }

    fun fromAdJson(json: String): ForestAdBean {
        val gson = Gson()
        return gson.fromJson(json, ForestAdBean::class.java)
    }

    fun fromLogicJson(json: String): ForestLogicBean {
        val gson = Gson()
        return gson.fromJson(json, ForestLogicBean::class.java)
    }

    fun getAdmobIdList(adList: String): Array<String> {
        return adList.split(",").toTypedArray()
    }

    fun setShowNum() {
        var num = kv.decodeInt(local_s_n, 0)
        num += 1
        kv.encode(local_s_n, num)
    }

    fun setClickNum() {
        var num = kv.decodeInt(local_c_n, 0)
        num += 1
        kv.encode(local_c_n, num)
    }

    fun getShowNum(): Int {
        return kv.decodeInt(local_s_n, 0)
    }

    fun getClickNum(): Int {
        return kv.decodeInt(local_c_n, 0)
    }


    fun setAdLoadDateNum(data: String) {
        kv.encode(ad_load_date, data)
    }

    fun getAdLoadDateNum(): String {
        return kv.decodeString(ad_load_date, "") ?: ""
    }


    interface ResponseCallback {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    private fun getAppVersion(): String? {
        return try {
            val packageInfo =
                RedApp.redApp.packageManager.getPackageInfo(RedApp.redApp.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun blackData(): Map<String, Any> {
        return mapOf(
            "bathos" to "com.sunrise.fast.secure.link.infinity",
            "dynamic" to "codify",
            "taper" to getAppVersion().orEmpty(),
            "prior" to "userIdKey".getKv()
        )
    }

    fun getBlackList(context: Context) {
        if (blockData.getKv().isNotBlank()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            postMapData(
                "https://jerry.sunrisefast.com/gumption/radish",
                blackData(),
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        log("The blacklist request is successful：$response")
                        response.putKv(blockData)
                    }

                    override fun onFailure(error: String) {
                        GlobalScope.launch(Dispatchers.IO) {
                            delay(10000)
                            log("The blacklist request failed：$error")
                            getBlackList(context)
                        }
                    }
                })
        }
    }

    private fun postMapData(
        url: String,
        map: Map<String, Any>,
        callback: ResponseCallback
    ) {
        val jsonBody = JSONObject(map).toString()  // Convert map to JSON string

        val client = OkHttpClient()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onFailure("HTTP error: ${response.code}")
                        return
                    }

                    val responseBody = response.body?.string() ?: ""
                    callback.onSuccess(responseBody)
                }
            }
        })
    }

}