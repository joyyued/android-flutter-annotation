package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StandardMessageCodec

/**
 * @author: Jiang PengYong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des: standard channel
 */
@FlutterBasicChannel(
    codecClass = StandardMessageCodec::class,
    channelName = Config.STANDER_BASIC_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaStandardBasicChannel {

    @Receive
    fun receiverFromFlutter(
        receiver: Any?,
        @Callback reply: BasicMessageChannel.Reply<Any>
    ) {
        val map = receiver as? HashMap<*, *> ?: return
        val name = map["name"]
        val weight = map["weight"]
        Log.e(
            "Neza", "[Basic standard channel receiver]" +
                    " receiverFromFlutter(name: $name, weight: $weight)"
        )
    }
}