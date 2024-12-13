package com.light.lightV.indigo

import com.light.lightV.BuildConfig
import com.light.lightV.green.getKv
import com.light.lightV.green.lightVDebugLog
import com.light.lightV.green.putKv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.UUID

val tbaWebsite =
    if (BuildConfig.DEBUG) "https://test-cache.sunrisefast.com/coventry/haines"
    else "https://cache.sunrisefast.com/suffice/boletus"


val normalWeb: JSONObject
    get() {
        if ("userIdKey".getKv().isEmpty()) {
            UUID.randomUUID().toString().putKv("userIdKey")
        }
        return JSONObject().apply {
            put("simmer", JSONObject().apply {
                put("length", System.currentTimeMillis().toString())
                put("aaas", UUID.randomUUID().toString())
                put("prior", "userIdKey".getKv())
                put("devious", Locale.getDefault().language + "_" + Locale.getDefault().country)
            })
            put("fleming", JSONObject().apply {
                put("endogamy", "001")
                put("bathos", "com.sunrise.fast.secure.link.infinity")
                put("beecham", "001")
                put("goblet", "001")
                put("laurie", "001")

                put("rascal", "001")
                put("dynamic", "codify")
                put("taper", BuildConfig.VERSION_NAME)
            })

        }
    }


fun sendPoint(eventName: String, value: String, reLoad: Int = 10) {
    runCatching {
        val client = OkHttpClient.Builder().build()
        val json = normalWeb.apply {
            put("knox", eventName)
            put("uu&katie", value)
        }.toString()
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        "101 point parmer ===>${json}".lightVDebugLog()
        client.newCall(
            Request.Builder().url(tbaWebsite)
                .post(requestBody)
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                "101 point result ===>fail".lightVDebugLog()
                if (reLoad > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(2000)
                        withContext(Dispatchers.Main) {
                            sendPoint(eventName, value, reLoad - 1)
                        }
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                val code = response.code
                "101 point result ===>${code}  ${result}".lightVDebugLog()
                if (code != 200) {
                    if (reLoad > 0) {
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            withContext(Dispatchers.Main) {
                                sendPoint(eventName, value, reLoad - 1)
                            }
                        }
                    }
                }
            }
        })
    }.onFailure {
        if (reLoad > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                withContext(Dispatchers.Main) {
                    sendPoint(eventName, value, reLoad - 1)
                }
            }
        }
    }
}

