package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec

/**
 * @author: Jiang PengYong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des: string channel
 */
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = Config.STRING_BASIC_CHANNEL_NONE_SENDER,
    type = ChannelType.RECEIVER
)
class NezaStringBasicChannelNoneSender {

    @Receive
    fun test(
        json: String?,
        @Callback reply: BasicMessageChannel.Reply<String>
    ) {
        Log.e(
            "Neza", "[test] " +
                    "receiverJsonFromFlutter(json: $json)"
        )
    }
}