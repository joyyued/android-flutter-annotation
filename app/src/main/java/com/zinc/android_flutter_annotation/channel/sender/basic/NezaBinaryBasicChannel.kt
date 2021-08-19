package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BinaryCodec
import io.flutter.plugin.common.JSONMessageCodec
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.common.StringCodec
import java.nio.ByteBuffer

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = BinaryCodec::class,
    channelName = Config.BINARY_BASIC_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaBinaryBasicChannel {
    fun sendBinary(@Param b: ByteBuffer)
}