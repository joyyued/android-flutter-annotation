package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.annotation.common.Callback
import com.joyy.annotation.model.ChannelType
import com.joyy.annotation.method.FlutterMethodChannel
import com.joyy.annotation.method.HandleMessage
import com.joyy.annotation.method.ParseData
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.MethodChannel

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:19 上午
 * @email: 56002982@qq.com
 * @des: Method Channel
 */
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = Config.METHOD_CHANNEL_NONE_SENDER
)
class NezaMethodChannelNoneSender {

    @Callback
    var result: MethodChannel.Result? = null

    @HandleMessage
    fun test() {
        show("test")
    }

    private fun show(msg: String) {
        Log.i("Neza", "[Method channel receiver] $msg")
    }
}