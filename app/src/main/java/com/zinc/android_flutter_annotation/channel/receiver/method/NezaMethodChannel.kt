package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:19 上午
 * @email: 56002982@qq.com
 * @des: Method Channel
 */
object NezaMethodChannel {
    fun sayHelloToNative() {
        Log.e("NezaMethodChannel", "[Flutter -> Native]sayHelloToNative")
    }
}