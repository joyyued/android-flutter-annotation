package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.method.HandleMessage
import com.joyy.neza_annotation.method.ParseData
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

    @Callback
    var result: MethodChannel.Result? = null

    @HandleMessage
    fun sayHelloToNative() {
        show("sayHelloToNative")
    }

    @ParseData
    @HandleMessage
    fun sayHelloToNativeWithParam(name: String?, age: Int?) {
        show("sayHelloToNativeWithParam(name: $name, age: $age)")
        result?.success("receiver success[name: $name, $age]")
    }

    @HandleMessage
    fun sayHelloToNativeWithRaw(map: Any) {
        show("sayHelloToNativeWithRaw(map: $map)")
        result?.error("100", "receiver error[$map]", name)
    }

    private fun show(msg: String) {
        Log.i("Neza", "[Method channel receiver] $msg")
    }
}