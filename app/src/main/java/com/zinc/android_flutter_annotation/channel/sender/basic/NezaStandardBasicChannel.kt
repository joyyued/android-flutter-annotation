package com.zinc.android_flutter_annotation.channel.sender.basic

import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Param
import com.joyy.ued.android_flutter_annotation.annotation.common.ParamMap
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import com.zinc.android_flutter_annotation.config.Config
import io.flutter.plugin.common.StandardMessageCodec

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 4:02 下午
 * @email: 56002982@qq.com
 * @des: standard channel sender
 */
@FlutterBasicChannel(
    codecClass = StandardMessageCodec::class,
    channelName = Config.STANDER_BASIC_CHANNEL,
    type = ChannelType.SENDER
)
interface NezaStandardBasicChannel {
    @Param
    fun sendToFlutter()

    @ParamMap
    fun sendToFlutterMap()

    @Param
    fun sendToFlutter(age: Int)

    @ParamMap
    fun sendToFlutterMap(age: Int)

    @Param
    fun sendToFlutter(
        map: HashMap<String, String>
    )

    @Param
    fun sendToFlutterMapList(
        map: HashMap<String, ArrayList<String>>
    )

    @Param
    fun sendToFlutterByteArray(
        map: HashMap<String, ByteArray>
    )

    @Param
    fun sendToFlutter(
        name: String,
        age: Int
    )

    @ParamMap
    fun sendToFlutter(
        name: String,
        age: Int,
        height: Int,
        byteArray: ByteArray
    )
}