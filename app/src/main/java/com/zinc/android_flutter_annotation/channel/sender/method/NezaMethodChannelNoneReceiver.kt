package com.zinc.android_flutter_annotation.channel.sender.method

import com.joyy.ued.android_flutter_annotation.annotation.common.Param
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config

@FlutterMethodChannel(
    type = ChannelType.SENDER,
    channelName = Config.METHOD_CHANNEL_NONE_RECEIVER
)
interface NezaMethodChannelNoneReceiver {
    @Param
    fun test()
}
