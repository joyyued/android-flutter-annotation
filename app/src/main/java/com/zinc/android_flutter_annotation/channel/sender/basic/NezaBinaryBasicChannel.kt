package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Param
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BinaryCodec
import java.nio.ByteBuffer

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des: Binary channel sender
 */
@FlutterBasicChannel(
    codecClass = BinaryCodec::class,
    channelName = Config.BINARY_BASIC_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaBinaryBasicChannel {
    @Param
    fun sendBinary(b: ByteBuffer)
}