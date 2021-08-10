package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des:
 */
object NezaBasicChannel {
    fun receiverJsonFromFlutter(json: String) {
        Log.e("NezaBasicChannel", "[Flutter -> Native]$json")
    }
}