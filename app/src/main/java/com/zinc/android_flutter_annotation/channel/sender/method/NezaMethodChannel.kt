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

    fun sayHelloToFlutter(): Deferred<MethodChannelResult>

    fun sayHelloToFlutter(
        @Param hashMap: HashMap<String, String>
    )

//    fun sayHelloToFlutter(
//        @Param hashMap: HashMap<String, Any?>
//    )


    fun sayHelloToFlutter(
        @Param name: String
    )

    fun sayHelloToFlutter(
        @ParamMap name: String,
        @ParamMap age: Int,
        @ParamMap height: Int?
    )

    fun sayHelloToFlutter(
        @ParamMap name: String,
        @ParamMap weight: Int,
    )
}
