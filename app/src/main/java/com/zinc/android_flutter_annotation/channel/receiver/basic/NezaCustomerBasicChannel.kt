package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.common.Callback
import com.joyy.annotation.method.HandleMessage
import com.joyy.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.codec.HashMapMessageCodec
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.BasicMessageChannel

/**
 * @author: Jiang Pengyong
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

    @Callback
    var reply: BasicMessageChannel.Reply<HashMap<String, String>>? = null

    @HandleMessage
    fun receiverJsonFromFlutter(map: HashMap<String, String>?) {
        Log.e("Neza", "[Basic customer channel receiver] receiverJsonFromFlutter(map: $map)")
    }
}