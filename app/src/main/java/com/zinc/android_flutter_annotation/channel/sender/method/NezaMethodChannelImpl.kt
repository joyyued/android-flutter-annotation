package com.zinc.android_flutter_annotation.channel.sender.method

import com.zinc.android_flutter_annotation.channel.receiver.method.NezaMethodChannelProxy

object NezaMethodChannelImpl : NezaMethodChannel {
    // TODO No:1
    override fun sayHelloToFlutter() {

        NezaMethodChannelProxy.instance
            .getChannel()
            ?.invokeMethod("sayHelloToFlutter", null, null)
    }

    suspend fun sayHelloToFlutter(map: HashMap<String, Any>) {
    }
}
