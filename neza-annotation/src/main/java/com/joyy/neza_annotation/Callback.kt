package com.joyy.neza_annotation

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/13 3:18 下午
 * @email: 56002982@qq.com
 * @des: 用于标记回调，此标记可以用于两处地方：
 * 1、在 Method Channel 的接收者中使用，即 @FlutterMethodChannel(type=ChannelType.RECEIVER)
 * 的类中使用，并且只能标记于 io.flutter.plugin.common.MethodChannel.Result 的类型上，否则报错
 * 2、在 Basic Message Channel 的接收者中使用，即 @FlutterBasicChannel(type=ChannelType.RECEIVER)
 * 的类中使用，并且只能标记于 io.flutter.plugin.common.BasicMessageChannel.Reply<T> 的类型上，否则报错
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Callback 