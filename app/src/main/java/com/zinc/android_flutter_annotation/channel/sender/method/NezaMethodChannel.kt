package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.ued.android_flutter_annotation.annotation.common.Param
import com.joyy.ued.android_flutter_annotation.annotation.common.ParamMap
import com.joyy.ued.android_flutter_annotation.annotation.common.Send
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
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

    @Send("method/change_name")
    fun changeName()
}
