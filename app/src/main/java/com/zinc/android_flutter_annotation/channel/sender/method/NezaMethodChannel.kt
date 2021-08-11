package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.neza_annotation.MethodChannelType
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.zinc.android_flutter_annotation.config.ChannelConfig

@FlutterMethodChannel(
    type = MethodChannelType.SENDER,
    channelName = ChannelConfig.METHOD_CHANNEL
)
interface NezaMethodChannel {

    fun sayHelloToFlutter()
}