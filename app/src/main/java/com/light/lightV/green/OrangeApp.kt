package com.light.lightV.green

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.light.lightV.BuildConfig
import com.light.lightV.R
import com.light.lightV.purple.YellowDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


fun String.lightVDebugLog(before: String? = null, after: String? = null) {
    var result = this
    if (!before.isNullOrEmpty()) result = before + result
    if (!after.isNullOrEmpty()) result += after
    if (BuildConfig.DEBUG) Log.e("LightVDebugLog", result)
}

fun String.putKv(name: String) {
    kv.encode(name, this)
}

fun String.getKv(): String {
    return kv.decodeString(this, "") ?: ""
}

fun Int.putKv(name: String) {
    kv.encode(name, this)
}

fun Int.getKv(name: String): Int {
    return kv.decodeInt(name, 0)
}

fun String.toDayLimitKey(): String {
    return SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
}

fun String.toNow(plus: String = ""): String {
    return SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()) + plus
}

fun getIntFromString(key: String): Int {
    when (key) {
        "AU" -> return R.mipmap.australia
        "BE" -> return R.mipmap.belgium
        "BR" -> return R.mipmap.brazil
        "CA" -> return R.mipmap.canada
        "FR" -> return R.mipmap.france
        "DE" -> return R.mipmap.germany
        "HK" -> return R.mipmap.hongkong
        "IN" -> return R.mipmap.india
        "IE" -> return R.mipmap.ireland
        "IT" -> return R.mipmap.italy
        "JP" -> return R.mipmap.japan
        "KR" -> return R.mipmap.koreasouth
        "NL" -> return R.mipmap.netherlands
        "NZ" -> return R.mipmap.newzealand
        "NO" -> return R.mipmap.norway
        "RU" -> return R.mipmap.russianfederation
        "SG" -> return R.mipmap.singapore
        "SE" -> return R.mipmap.sweden
        "CH" -> return R.mipmap.switzerland
        "AE" -> return R.mipmap.unitedarabemirates
        "GB" -> return R.mipmap.unitedkingdom
        "US" -> return R.mipmap.unitedstates
        else -> return R.mipmap.smart_sever_logo
    }
}


fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCapabilities = connectivityManager.getNetworkCapabilities(
        connectivityManager.activeNetwork
    )
    if (networkCapabilities != null) {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    return false
}

fun isNetworkConnected(context: Context,nextFUn:()->Unit): Boolean {
    val mConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val mNetworkInfo = mConnectivityManager.activeNetworkInfo
    if (mNetworkInfo != null) {
        return !mNetworkInfo.isAvailable
    }
    nextFUn()
    return true
}


var addressLimit = false


var isFetchingIPAddress = false

fun netLimit() {
    isFetchingIPAddress = true
    OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build().newCall(Request.Builder().url(globalCheckIpUrl).build())
        .enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ("IP State fetch error " + e.message).lightVDebugLog()
                isFetchingIPAddress = false
                val limit = noVpnCountries.find {
                    Locale.getDefault().country.contains(
                        it,
                        true
                    )
                } != null
                ("Ip limit ${limit}   ${Locale.getDefault().country}").lightVDebugLog()
                addressLimit = limit
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                isFetchingIPAddress = false
                if (result != null) {
                    runCatching {
                        val jsonObject = JSONObject(result)
                        val currentCountryCode = jsonObject.optString("country")
                        ("IP State  net limit  $result    $currentCountryCode").lightVDebugLog()

                        if (currentCountryCode.isEmpty()) {
                            addressLimit = noVpnCountries.find {
                                Locale.getDefault().country.contains(
                                    it,
                                    true
                                )
                            } != null
                            "Ip limit ${addressLimit}   ${Locale.getDefault().country}".lightVDebugLog()
                        } else {
                            addressLimit = noVpnCountries.contains(currentCountryCode)
                        }
                    }
                }
            }
        })
}

val noVpnCountries = arrayListOf("HK", "MO", "CN", "IR")
//val noVpnCountries = arrayListOf("MO", "IR")