package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.codec.HashMapMessageCodec
import com.zinc.android_flutter_annotation.config.Config

/**
 * @author: Jiang PengYong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des: customer channel sender
 */
@FlutterBasicChannel(
    codecClass = HashMapMessageCodec::class,
    channelName = Config.BINARY_CUSTOMER_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaCustomerBasicChannel {
    fun sendMapToFlutter(map: HashMap<String, String>)
}