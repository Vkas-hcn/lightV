package com.light.lightV.blue

class YellowMob(
    private var idList: ArrayList<String>,
    private var idIndex: Int,
) {
    fun getId(): String {
        return idList[idIndex]
    }

    fun reset() {
        idIndex = 0
    }

    fun next() {
        idIndex += 1
        if (idIndex > idList.size - 1) {
            reset()
        }
    }

    fun getTopLoadTime(isOpen: Boolean = false): Int {
        return if (idList.isEmpty()) 3
        else {
            if (isOpen) idList.size * 2
            else idList.size
        }
    }
}