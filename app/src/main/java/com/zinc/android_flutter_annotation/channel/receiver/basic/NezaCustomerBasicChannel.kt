package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.method.HandleMessage
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.codec.HashMapMessageCodec
import com.zinc.android_flutter_annotation.codec.StringCodec
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
//    codecClass = StandardMessageCodec::class,
    codecClass = HashMapMessageCodec::class,
    channelName = Config.BINARY_CUSTOMER_CHANNEL,
    type = ChannelType.RECEIVER
)
class NezaCustomerBasicChannel {

    @Callback
    var reply: BasicMessageChannel.Reply<HashMap<String, String>>? = null
//    var reply: BasicMessageChannel.Reply<Any>? = null

    @HandleMessage
    fun receiverJsonFromFlutter(map: HashMap<String, String>?) {
//    fun receiverJsonFromFlutter(map: Any?) {
        Log.e("NezaBasicChannel", "[Flutter -> Native] Receiver from customer codec: $map")
    }
}