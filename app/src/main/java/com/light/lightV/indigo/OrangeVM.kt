package com.light.lightV.indigo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrangeVM : ViewModel() {

    val handConnect: MutableLiveData<Boolean> = MutableLiveData()


    fun doHandConnect() {
        handConnect.postValue(true)
    }


    val canSwitchTab: MutableLiveData<Boolean> = MutableLiveData()


    var canClick = true
    var canBack = true
    fun canClickTab(value: Boolean) {
        canSwitchTab.postValue(value)
    }


    val switchSeverConnect: MutableLiveData<Boolean> = MutableLiveData()

    fun switchSever(value: Boolean) {
        switchSeverConnect.postValue(value)
    }


    val updateNoVpnPackages: MutableLiveData<Boolean> = MutableLiveData()

    fun updateNoVpnPs() {
        updateNoVpnPackages.postValue(true)
    }

    val afterSwitchTabUpdateNoVpnP: MutableLiveData<Boolean> = MutableLiveData()
}