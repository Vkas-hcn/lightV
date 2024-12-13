package com.light.lightV.green

import com.light.lightV.BuildConfig

//val redDebugConfig = """{
//    "actionttttttt": "3spacer300",
//    "rusty": "ca-app-pub-3940256099942544/9257395921&ca-app-pub-3940256099942544/9257395921xxx&ca-app-pub-3940256099942544/9257395921",
//    "usage": "ca-app-pub-3940256099942544/2247696110&ca-app-pub-3940256099942544/2247696110&ca-app-pub-3940256099942544/2247696110",
//    "round": "ca-app-pub-3940256099942544/2247696110&ca-app-pub-3940256099942544/2247696110&ca-app-pub-3940256099942544/2247696110",
//    "yowza": "ca-app-pub-3940256099942544/1033173712&ca-app-pub-3940256099942544/1033173712&ca-app-pub-3940256099942544/1033173712",
//    "badly": "ca-app-pub-3940256099942544/1033173712&ca-app-pub-3940256099942544/1033173712&ca-app-pub-3940256099942544/1033173712"
//}"""


val redDebugConfig = """{
    "actionttttttt": "300spacer3",
    "rusty": "ca-app-pub-3940256099942544/9257395921",
    "usage": "ca-app-pub-3940256099942544/2247696110",
    "round": "ca-app-pub-3940256099942544/2247696110",
    "yowza": "ca-app-pub-3940256099942544/1033173712",
    "badly": "ca-app-pub-3940256099942544/1033173712",
    "jens": "ca-app-pub-3940256099942544/1033173712"
}"""


val yellowDebugConfig = """{
    "ouch": "1",
    "dda":"",
    "eed": "10spacer10"
}"""

val redReleaseConfig = """{
    "actionttttttt": "300spacer3",
    "rusty": "ca-app-pub-1161741210279999/9474470969",
    "usage": "ca-app-pub-1161741210279999/8161389298",
    "round": "ca-app-pub-1161741210279999/8863262722",
    "yowza": "ca-app-pub-1161741210279999/5917503366",
    "badly": "ca-app-pub-1161741210279999/5535225954",
    "jens": "ca-app-pub-3940256099942544/1033173712"
}"""

val yellowReleaseConfig = """{
    "ouch": "1",
    "dda":"",
    "eed": "10spacer10"
}"""


fun getRealAdConfig(): String {
    return if (BuildConfig.DEBUG) redDebugConfig
    else {
        "under".getKv().ifEmpty {
            redReleaseConfig
        }
    }
}

fun getRealLimitConfig(): String {
    return if (BuildConfig.DEBUG) yellowDebugConfig
    else {
        "often".getKv().ifEmpty {
            yellowReleaseConfig
        }
    }
}


const val globalCheckIpUrl = "https://ipapi.co/json"
