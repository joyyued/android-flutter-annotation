package com.joyy.neza_annotation.basic

import com.joyy.neza_annotation.model.ChannelType
import kotlin.reflect.KClass

/**
 * @author: Jiang Pengyong
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