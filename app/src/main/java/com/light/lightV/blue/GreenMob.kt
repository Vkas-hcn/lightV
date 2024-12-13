package com.light.lightV.blue

enum class MobName {
    LaunchOpen, NativeHomeTop, NativeResultBottom, InterstitialConnect, InterstitialServerBack, InterstitialResultBack
}

enum class MobType {
    Open, Native, Interstitial
}

enum class MobState {
    Empty, Getting, GetFail, GetSuccess, Showing, ShowFail, ShowFinish, ClickShowing
}