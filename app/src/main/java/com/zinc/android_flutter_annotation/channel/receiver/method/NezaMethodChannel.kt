package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.neza_annotation.MethodChannelType
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.zinc.android_flutter_annotation.config.ChannelConfig

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:19 上午
 * @email: 56002982@qq.com
 * @des: Method Channel
 */
@FlutterMethodChannel(
    type = MethodChannelType.RECEIVER,
    channelName = ChannelConfig.METHOD_CHANNEL
)
object NezaMethodChannel {
    fun sayHelloToNative() {
        Log.e("NezaMethodChannel", "[Flutter -> Native]sayHelloToNative")
    }
}