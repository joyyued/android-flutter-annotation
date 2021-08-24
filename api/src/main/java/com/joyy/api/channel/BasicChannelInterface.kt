package com.joyy.api.channel

import android.content.Context
import io.flutter.plugin.common.BasicMessageChannel

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:22 上午
 * @email: 56002982@qq.com
 * @des: Flutter 的 method channel 接口
 */
interface BasicChannelInterface<T> {
    fun init(context: Context)

    fun getChannel(): BasicMessageChannel<T>?

    fun getChannelName(): String
}