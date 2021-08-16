package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.method.RawData
import com.zinc.android_flutter_annotation.config.Config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:19 上午
 * @email: 56002982@qq.com
 * @des: Method Channel
 */
//@FlutterEngine(engineId = "江澎涌")
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = Config.METHOD_CHANNEL
)
object NezaMethodChannel {
    fun sayHelloToNative() {
        Log.e("NezaMethodChannel", "[Flutter -> Native]sayHelloToNative")
    }

    fun sayHelloToNativeWithParam(a: Int?) {
    }

    fun sayHelloToNativeWithRaw(@RawData a: Any) {
    }

//    fun sayHelloToNativeWithRawError(@RawData a: Int) {
//    }
}