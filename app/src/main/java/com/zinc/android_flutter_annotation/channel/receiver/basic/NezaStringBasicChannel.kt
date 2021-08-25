package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.common.Callback
import com.joyy.annotation.method.HandleMessage
import com.joyy.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des: string channel
 */
//@FlutterBasicChannel(
//    codecClass = StringCodec::class,
//    channelName = Config.STRING_BASIC_CHANNEL,
//    type = ChannelType.RECEIVER
//)
class NezaStringBasicChannel {

    @Callback
    var reply: BasicMessageChannel.Reply<String>? = null

    @HandleMessage
    fun receiverJsonFromFlutter(json: String?) {
        Log.e(
            "Neza", "[Basic string channel receiver] " +
                    "receiverJsonFromFlutter(json: $json)"
        )
    }
}