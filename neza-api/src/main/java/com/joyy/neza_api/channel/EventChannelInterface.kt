package com.joyy.neza_api.channel

import android.content.Context
import io.flutter.plugin.common.EventChannel

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 2:48 下午
 * @email: 56002982@qq.com
 * @des: Flutter 的 method channel 接口
 */
interface EventChannelInterface {

    fun init(context: Context)

    fun getChannel(): EventChannel?

    fun getChannelName(): String

    fun getEventSink(): EventChannel.EventSink?
}