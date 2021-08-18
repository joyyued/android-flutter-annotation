package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.basic.MessageHandler
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StandardMessageCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = StandardMessageCodec::class,
    channelName = Config.STANDER_BASIC_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaStandardBasicChannel {

    @Callback
    var reply: BasicMessageChannel.Reply<Any>? = null

    @MessageHandler
    fun receiverFromFlutter(receiver: Any?) {
        val map = receiver as? HashMap<*, *> ?: return
        val name = map["name"]
        val weight = map["weight"]
        Log.e("NezaBasicChannel", "[Flutter -> Native] standard: $name $weight")
    }
}