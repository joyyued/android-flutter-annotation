package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.channel.StringCodec
import com.zinc.android_flutter_annotation.config.Config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = Config.STRING_BASIC_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaStringBasicChannel {
    fun sendJsonToFlutter(json: String)
}