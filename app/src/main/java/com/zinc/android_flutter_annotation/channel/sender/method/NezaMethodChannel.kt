package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_annotation.model.MethodChannelResult
import com.zinc.android_flutter_annotation.config.Config
import kotlinx.coroutines.Deferred

@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = Config.METHOD_CHANNEL
)
interface NezaMethodChannel {

    @Param
    fun sayHelloToFlutter(): Deferred<MethodChannelResult>

//    @ParamMap
//    fun sayHelloToFlutter(
//        hashMap: HashMap<String, String>
//    )

    @Param
    fun sayHelloToFlutter(
        hashMap: HashMap<String, Any?>
    )

    @ParamMap
    fun sayHelloToFlutter(
        name: String
    )

    @Param
    fun sayHelloToFlutter(
        name: String,
        age: Int,
        height: Int?
    )

    @ParamMap
    fun sayHelloToFlutter(
        name: String,
        weight: Int,
    )
}
