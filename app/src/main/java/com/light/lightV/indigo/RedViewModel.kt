package com.light.lightV.indigo

import android.util.Base64
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.google.gson.Gson
import com.light.lightV.BuildConfig
import com.light.lightV.blue.ggggg.AdUtils
import com.light.lightV.green.getKv
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.putKv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.log

val serverUrl =
    if (BuildConfig.DEBUG) "https://test.sunrisefast.com/cnMrENXa/CqDbos/HvlxVare/"
    else "https://api.sunrisefast.com/cnMrENXa/CqDbos/HvlxVare/"

fun updateSeversConfig(content: String) {
    appSeverLists = Gson().fromJson(decodeNewSecret(content), Babiesver::class.java)
    olderServer.clear()
    appSeverLists?.data?.severList?.forEach { oneSever ->
        if (olderServer.filter { it.countryCode == oneSever.countryCode }
                .isNotEmpty()) {
            olderServer.find { it.countryCode == oneSever.countryCode }?.detailList?.add(
                oneSever
            )
        } else {
            olderServer.add(
                SeverCountryContainer(
                    countryCode = oneSever.countryCode,
                    countryName = oneSever.countryName,
                    detailList = arrayListOf(oneSever)
                )
            )
        }
    }

    if (currentSelectSever == null) {
        changeSever(appSeverLists?.data?.smartList?.random(), true)
    }
}

var isLoadingSever = false
fun loadSevers() {
    isLoadingSever = true
    "start request".lightVDebugLog()
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    val request = Request.Builder().url(serverUrl)
        .addHeader("XXDZ", "ZZ")
        .addHeader(
            "FKJ",
            if (BuildConfig.DEBUG) "com.light.lightV" else "com.sunrise.fast.secure.link.infinity"
        )
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            "load sever fail ${e.message.toString()}".lightVDebugLog()
            isLoadingSever = false
            val cacheSever = "seversSecretString".getKv()
            if (cacheSever.isNotEmpty()) {
                updateSeversConfig(cacheSever)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string()
            isLoadingSever = false
            "load sever success ${result}".lightVDebugLog()
            "load sever success ${decodeNewSecret(result ?: "")}".lightVDebugLog()
            if (result != null) {
                result.putKv("seversSecretString")
                updateSeversConfig(result)
            } else {
                val cacheSever = "seversSecretString".getKv()
                if (cacheSever.isNotEmpty()) {
                    updateSeversConfig(cacheSever)
                }
            }
        }
    })
}


fun decodeNewSecret(content: String): String {
    try {
        return String(
            Base64.decode(
                content.substring(0, content.length - 26).reversed(),
                Base64.DEFAULT
            ), StandardCharsets.UTF_8
        )
    } catch (e: Exception) {
        e.message.toString().lightVDebugLog()
        return ""
    }
}

var appSeverLists: Babiesver? = null
var olderServer: ArrayList<SeverCountryContainer> = arrayListOf()


var currentSelectSever: WaterBt? = null
var vcurrentSelectSeverIsSmart = false

fun changeSever(entity: WaterBt?, isSmart: Boolean = true) {
    if (entity == null) return
    "change Sever ${entity.ip}   ${entity.port}   ${entity.password}  ${entity.addSecretWay}".lightVDebugLog()
    val profile = ProfileManager.createProfile(
        Profile(
            name = "Sunny",
            host = entity.ip,
            remotePort = entity.port,
            password = entity.password,
            method = entity.addSecretWay
        )
    )
    vcurrentSelectSeverIsSmart = isSmart
    currentSelectSever = entity
    Core.switchProfile(profile.id)
}


fun loadAdminType() {
    if ("adminType".getKv().isNotEmpty()) {
        return
    }

    "start loadAdminType request".lightVDebugLog()
    val client = OkHttpClient.Builder().build()
    val adminUrl =
        "https://jerry.sunrisefast.com/gumption/radish?bathos=com.sunrise.fast.secure.link.infinity&dynamic=codify&taper=${BuildConfig.VERSION_NAME}&length=${System.currentTimeMillis()}"
    "admin type get===>${adminUrl}".lightVDebugLog()
    client.newCall(Request.Builder().url(adminUrl).build()).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(10000)
                loadAdminType()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string()
            "admin type result ==>${result}".lightVDebugLog()
            if (!result.isNullOrEmpty()) {
                result.putKv("adminType")
            }
        }
    })
}

