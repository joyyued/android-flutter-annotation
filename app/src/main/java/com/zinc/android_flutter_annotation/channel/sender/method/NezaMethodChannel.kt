package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config

@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = Config.METHOD_CHANNEL
)
interface NezaMethodChannel {

    @Param
    fun sayHelloToFlutter()

    @Param
    fun sayHelloToFlutter(
        hashMap: HashMap<String, Any?>
    )

    /**
     * 会组合成 HashMap 传递
     */
    @ParamMap
    fun sayHelloToFlutter(
        name: String
    )

    /**
     * 只会使用到 name 参数
     */
    @Param
    fun sayHelloToFlutter(
        name: String,
        age: Int,
        height: Int?
    )

    @ParamMap
    fun sayHelloToFlutter(
        name: String,
        weight: Int
    )
}
