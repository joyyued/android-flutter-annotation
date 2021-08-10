package com.zinc.android_flutter_annotation.channel.sender.method

import com.zinc.android_flutter_annotation.channel.receiver.method.NezaMethodChannelProxy

object NezaMethodChannelImpl : NezaMethodChannel {
    override fun sayHelloToFlutter() {
        NezaMethodChannelProxy.instance
            .getChannel()
            ?.invokeMethod("sayHelloToFlutter", null)
    }
}