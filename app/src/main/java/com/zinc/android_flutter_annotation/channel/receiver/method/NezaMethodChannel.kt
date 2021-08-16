package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.neza_annotation.Callback
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.method.Method
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

    fun sayHelloToNative() {
        Log.e("NezaMethodChannel", "[Flutter -> Native]sayHelloToNative")
    }

    @ParseData
    fun sayHelloToNativeWithParam(name: String?, age: Int?) {
        result?.success("receiver success[name: $name, $age]")
    }

    fun sayHelloToNativeWithRaw(map: Any) {
        result?.error("100", "receiver error[$map]", name)
    }

    @Method("methodNewName")
    fun methodTest(){
        Log.e("NezaMethodChannel", "[Flutter -> Native] methodTest ")
    }
}