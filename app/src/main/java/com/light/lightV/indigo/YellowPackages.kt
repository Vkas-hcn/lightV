package com.light.lightV.indigo

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.light.lightV.green.RedApp.Companion.redApp
import com.light.lightV.green.lightVDebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

var isDealPackage = false
var allPackage: ArrayList<PackageAppMsgEntity> = arrayListOf()
fun getPackages() {
    isDealPackage = true
    CoroutineScope(Dispatchers.IO).launch {
        var storeStr = ""
        val packageManager: PackageManager = redApp.applicationContext.packageManager
        val packages = packageManager.getInstalledPackages(0)
        val appList = ArrayList<PackageAppMsgEntity>()

        var index = 0
        for (packageInfo in packages) {
            val appInfo = packageInfo.applicationInfo

            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                continue
            }

            val appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
            val packageName = packageInfo.packageName
            val appIcon = packageManager.getApplicationIcon(packageInfo.applicationInfo)

            if (appName != "Sunny" && !appName.startsWith("com")) {
                storeStr += "%${packageName}"
                appList.add(PackageAppMsgEntity(appName, packageName, appIcon, true, index))
                index += 1
            }
        }
        appList.sortBy { it.appNameSetOnUI.lowercase(Locale.ROOT) }
        isDealPackage = false
        allPackage = appList
        appList.toString().lightVDebugLog(before = "all packages")
    }
}