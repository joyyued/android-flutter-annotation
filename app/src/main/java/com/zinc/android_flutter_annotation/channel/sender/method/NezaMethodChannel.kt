package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.ChannelConfig

@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = ChannelConfig.METHOD_CHANNEL
)
interface NezaMethodChannel {

    fun sayHelloToFlutter()

    fun sayHelloToFlutterWithCallback(name: String, age: Int, height: Int?)
}