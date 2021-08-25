package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.JSONMessageCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des: json channel sender
 */
//@FlutterBasicChannel(
//    codecClass = JSONMessageCodec::class,
//    channelName = Config.BINARY_JSON_CHANNEL,
//    type = ChannelType.SENDER
//)
interface NezaJsonBasicChannel {
    fun sendJson(msg: String)
}