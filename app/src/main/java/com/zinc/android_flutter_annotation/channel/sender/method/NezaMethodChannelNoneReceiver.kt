package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.annotation.common.Param
import com.joyy.annotation.common.ParamMap
import com.joyy.annotation.method.FlutterMethodChannel
import com.joyy.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config

@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = Config.METHOD_CHANNEL_NONE_RECEIVER
)
interface NezaMethodChannelNoneReceiver {
    @Param
    fun test()
}
