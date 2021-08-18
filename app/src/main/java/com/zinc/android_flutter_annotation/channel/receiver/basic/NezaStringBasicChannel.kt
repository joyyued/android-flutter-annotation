package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.basic.MessageHandler
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.channel.StringCodec
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel


/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = Config.STRING_BASIC_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaStringBasicChannel {

    @Callback
    var reply: BasicMessageChannel.Reply<String>? = null

    @MessageHandler
    fun receiverJsonFromFlutter(json: String?) {
        Log.e("NezaBasicChannel", "[Flutter -> Native] $json")
    }
}