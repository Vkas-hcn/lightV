package com.light.lightV.indigo

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WaterBt(
    @SerializedName("EtkBpx")
    val userName: String,
    @SerializedName("gxXmCBuYk")
    val password: String,
    @SerializedName("mPejMzd")
    val addSecretWay: String,
    @SerializedName("LTZtikzFId")
    val cityName: String,
    @SerializedName("NhRlwHZw")
    val countryName: String,
    @SerializedName("XLuyJh")
    val signList: List<String>? = null,
    @SerializedName("Xbd")
    val countryCode: String,
    @SerializedName("XDNkRHBCOm")
    val mode: String,
    @SerializedName("BAduXyh")
    val ip: String,
    @SerializedName("fZWZsAy")
    val port: Int,

    var isComeFromSmartSeverList: Boolean = false,
)

@Keep
class BabyServer(
    @SerializedName("unzTQoS")
    val smartList: ArrayList<WaterBt>? = null,
    @SerializedName("gjyra")
    val severList: ArrayList<WaterBt>? = null,
)


@Keep
class SeverCountryContainer(
    val countryCode: String,
    val countryName: String,
    val detailList: ArrayList<WaterBt>? = null,
)

@Keep
class Babiesver(
    val data: BabyServer,
)


data class PackageAppMsgEntity(
    val appNameSetOnUI: String,
    val packageNameSetInSystem: String,
    val appIconSetOnUI: Drawable,
    var canUseVNet: Boolean,
    var index: Int,
)

