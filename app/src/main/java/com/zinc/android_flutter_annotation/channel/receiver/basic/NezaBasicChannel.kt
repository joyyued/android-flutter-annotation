package com.zinc.android_flutter_annotation.channel.receiver.basic

import android.util.Log
import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.ChannelConfig
import io.flutter.plugin.common.StringCodec
import java.lang.reflect.Type

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 3:44 下午
 * @email: 56002982@qq.com
 * @des:
 */
@FlutterBasicChannel(
    codecClass = StringCodec::class,
    channelName = ChannelConfig.EVENT_CHANNEL,
    type = ChannelType.RECEIVER
)
object NezaBasicChannel {
    fun receiverJsonFromFlutter(json: String) {
        Log.e("NezaBasicChannel", "[Flutter -> Native]$json")
    }
}