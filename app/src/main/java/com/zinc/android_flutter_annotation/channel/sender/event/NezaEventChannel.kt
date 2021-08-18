package com.zinc.android_flutter_annotation.channel.sender.event

import com.joyy.neza_annotation.common.Param
import com.joyy.neza_annotation.common.ParamMap
import com.joyy.neza_annotation.event.FlutterEventChannel
import com.zinc.android_flutter_annotation.config.Config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:28 下午
 * @email: 56002982@qq.com
 * @des: Event Channel
 */
//@FlutterEngine(engineId = "江澎涌")
@FlutterEventChannel(channelName = Config.EVENT_CHANNEL)
interface NezaEventChannel {
    fun sendImageInfo(
        @Param byteArray: ByteArray
    )

    fun sendImageInfo(
        @ParamMap id: Int,
        @ParamMap byteArray: ByteArray
    )

    fun sendImageInfo(
        @ParamMap id: Int,
        @ParamMap date: Long,
        @ParamMap byteArray: ByteArray
    )
}