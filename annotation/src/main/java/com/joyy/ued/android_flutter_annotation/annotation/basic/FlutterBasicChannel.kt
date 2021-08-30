package com.joyy.ued.android_flutter_annotation.annotation.basic

import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
import kotlin.reflect.KClass

/**
 * @author: Jiang PengYong
 * @date: 2021/8/13 3:19 下午
 * @email: 56002982@qq.com
 * @des: flutter basic channel 的注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FlutterBasicChannel(
    val codecClass: KClass<*>,
    val channelName: String,
    val type: ChannelType
)