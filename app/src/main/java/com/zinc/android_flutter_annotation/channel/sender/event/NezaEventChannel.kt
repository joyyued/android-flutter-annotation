package com.zinc.android_flutter_annotation.channel.sender.event

import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.event.FlutterEventChannel
import com.zinc.android_flutter_annotation.config.ChannelConfig

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:28 下午
 * @email: 56002982@qq.com
 * @des: Event Channel
 */
//@FlutterEngine(engineId = "江澎涌")
@FlutterEventChannel(channelName = ChannelConfig.EVENT_CHANNEL)
interface NezaEventChannel {
    fun sendImageInfo(byteArray: ByteArray)
    fun sendImageInfo(a: Int, byteArray: ByteArray)
    fun sendImageInfo(a: Int, b: String, byteArray: ByteArray)
}