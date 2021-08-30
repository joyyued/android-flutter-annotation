package com.zinc.android_flutter_annotation.channel.receiver.method

import android.util.Log
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.zinc.android_flutter_annotation.config.Config

/**
 * @author: Jiang PengYong
 * @date: 2021/8/10 11:19 上午
 * @email: 56002982@qq.com
 * @des: Method Channel
 */
@FlutterMethodChannel(
    type = ChannelType.RECEIVER,
    channelName = Config.METHOD_CHANNEL_NONE_SENDER
)
class NezaMethodChannelNoneSender {

    @Receive
    fun test() {
        show("test")
    }

    private fun show(msg: String) {
        Log.i("Neza", "[Method channel receiver] $msg")
    }
}