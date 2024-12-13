package com.github.shadowsocks.database

import com.tencent.mmkv.MMKV

object GlobalKv {
    private val kv by lazy { MMKV.mmkvWithID("GlobalMMKV", MMKV.MULTI_PROCESS_MODE) }

    fun String.getKv(): String {
        return kv.decodeString(this, "") ?: ""
    }
}

