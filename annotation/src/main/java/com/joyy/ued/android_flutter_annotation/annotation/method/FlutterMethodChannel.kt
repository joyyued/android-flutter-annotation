package com.joyy.ued.android_flutter_annotation.annotation.method

import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType

/**
 * @author: Jiang PengYong
 * @date: 2021/8/13 3:18 下午
 * @email: 56002982@qq.com
 * @des: flutter method channel 的注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FlutterMethodChannel(
    val type: ChannelType,
    val channelName: String
)