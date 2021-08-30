package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.codec.HashMapMessageCodec
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel

/**
 * @author: Jiang PengYong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des: 自定义 codec
 */
@FlutterBasicChannel(
    codecClass = HashMapMessageCodec::class,
    channelName = Config.BINARY_CUSTOMER_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaCustomerBasicChannel {
    @Receive
    fun receiverJsonFromFlutter(
        map: HashMap<String, String>?,
        @Callback reply: BasicMessageChannel.Reply<HashMap<String, String>>
    ) {
        Log.e("Neza", "[Basic customer channel receiver] receiverJsonFromFlutter(map: $map)")
        map?.put("platform", "Android")
        reply.reply(map)
    }
}