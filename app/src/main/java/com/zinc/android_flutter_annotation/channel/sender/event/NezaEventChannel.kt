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
    @Param
    fun sendImageInfo()

    @Param
    fun sendImageInfo(
        width: Int
    )

    @ParamMap
    fun sendImageInfo(
        byteArray: ByteArray
    )

    @ParamMap
    fun sendImageInfo(
        id: Int,
        byteArray: ByteArray
    )

    @ParamMap
    fun sendImageInfo(
        id: Int,
        date: Long,
        byteArray: ByteArray
    )

    fun sendUserInfo(
        name: String,
        age: Int
    )

    @Param
    fun sendUserInfo(
        name: String,
        age: Int,
        height: Int,
    )
}