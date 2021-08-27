package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.joyy.ued.android_flutter_annotation.annotation.method.HandleMessage
import com.joyy.ued.android_flutter_annotation.annotation.method.ParseData
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.MethodChannel

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
class NezaMethodChannel {

    var name: String = "jiang peng yong"

    @HandleMessage
    fun sayHelloToNative() {
        show("sayHelloToNative")
    }

    @HandleMessage
    fun sayHelloToNativeOnlyCallback(@Callback result: MethodChannel.Result) {
        show("sayHelloToNative")
    }

    @HandleMessage
    fun sayHelloToNativeWithoutCallback(any: Any) {
        show("sayHelloToNative")
    }

    @ParseData
    @HandleMessage
    fun sayHelloToNativeWithParam(
        name: String?,
        age: Int?,
        @Callback result: MethodChannel.Result
    ) {
        show("sayHelloToNativeWithParam(name: $name, age: $age)")
        result.success("receiver success[name: $name, $age]")
    }

    @HandleMessage
    fun sayHelloToNativeWithRaw(
        @Callback result1: MethodChannel.Result,
        map: Any,
        @Callback result2: MethodChannel.Result
    ) {
        show("sayHelloToNativeWithRaw(map: $map)")
        result1.error("100", "receiver error[$map]", name)
    }

    private fun show(msg: String) {
        Log.i("Neza", "[Method channel receiver] $msg")
    }
}