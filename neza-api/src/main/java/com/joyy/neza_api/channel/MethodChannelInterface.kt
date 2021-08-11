package com.joyy.neza_api.channel

import android.content.Context
import io.flutter.plugin.common.MethodChannel

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/10 11:22 上午
 * @email: 56002982@qq.com
 * @des: Flutter 的 method channel 接口
 */
interface MethodChannelInterface {
    fun init(context: Context)

    fun getChannel(): MethodChannel?

    fun getChannelName(): String
}