package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.ChannelConfig
import io.flutter.plugin.common.StringCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = ChannelConfig.EVENT_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaBasicChannel {

    fun sendJsonToFlutter(json: String)
}