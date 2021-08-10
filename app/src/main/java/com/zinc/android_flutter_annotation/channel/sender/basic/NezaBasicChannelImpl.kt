package com.zinc.android_flutter_annotation.channel.sender.basic

import com.zinc.android_flutter_annotation.channel.receiver.basic.NezaBasicChannelProxy

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:33 下午
 * @email: 56002982@qq.com
 * @des:
 */
object NezaBasicChannelImpl : NezaBasicChannel {
    override fun sendJsonToFlutter(json: String) {
        NezaBasicChannelProxy.instance.getChannel()
            ?.send(json)
    }
}