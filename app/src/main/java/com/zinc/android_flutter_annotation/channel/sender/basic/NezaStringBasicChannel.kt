package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.common.Param
import com.joyy.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.StringCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des: string channel sender
 */
//@FlutterBasicChannel(
//    codecClass = StringCodec::class,
//    channelName = Config.STRING_BASIC_CHANNEL,
//    type = ChannelType.SENDER
//)
interface NezaStringBasicChannel {
    @Param
    fun sendJsonToFlutter(json: String)
}